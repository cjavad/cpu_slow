import chisel3._
import chisel3.util._

class ControlUnit extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))

    // / kill
    val done = Output(Bool())

    // Interface with program counter
    val stop = Output(Bool())
    val jump = Output(Bool())
    val programCounterJump = Output(UInt(16.W))

    // Interface with dataMemory
    val dataMemoryReadData = Input(UInt(32.W))
    val dataMemoryAddress = Output(UInt(16.W))
    val dataMemoryWriteData = Output(UInt(32.W))
    val dataMemoryWriteEnable = Output(Bool())

    // Interface with registerFile
    val regA = Input(UInt(32.W))
    val regB = Input(UInt(32.W))
    val regSelA = Output(UInt(16.W))
    val regSelB = Output(UInt(16.W))
    val regWriteSel = Output(UInt(16.W))
    val regWriteData = Output(UInt(32.W))
    val regWriteEnable = Output(Bool())

    // Interface with ALU
    val aluSel = Output(UInt(5.W))
    val aluInA = Output(UInt(32.W))
    val aluInB = Output(UInt(32.W))
    val aluOut = Input(UInt(32.W))
    val aluComp = Input(Vec(5, Bool()))
    val aluCompOut0 = Input(Vec(5, Bool()))

  })

  // FLAGS
  // 0: LESSER
  // 1: EQUAL
  // 2: GREATER
  // 3: LESS OR EQUAL
  // 4: GREATER OR EQUAL

  val flags = RegInit(VecInit(Seq.fill(5)(0.B)))


  // Default signals
  io.done := 0.B

  io.stop := 0.B
  io.jump := 0.B
  io.programCounterJump := 0.U

  io.dataMemoryAddress := 0.U
  io.dataMemoryWriteData := 0.U
  io.dataMemoryWriteEnable := 0.B

  io.regSelA := 0.U
  io.regSelB := 0.U
  io.regWriteSel := 0.U
  io.regWriteData := 0.U
  io.regWriteEnable := 0.B

  io.aluSel := 0.U
  io.aluInA := 0.U
  io.aluInB := 0.U

  // SET instruction
  // 1dddddxxxxxxxxxxxxxxxxxxxxxxxxxx    set register = d, value = x
  when(io.instruction(0) === 1.U) {
    io.regWriteEnable := 1.B
    io.regWriteSel := io.instruction(5, 0)
    io.regWriteData := io.instruction(31, 6)
  }

  when(io.instruction(1, 0) === "b01".U) {
    /* ALU INSTRUCTIONS */
    io.aluSel := io.instruction(7, 2)
    // TODO, always ALU on regA and regB?
    io.aluInA := io.regA
    io.aluInB := io.regB

    io.regWriteSel := io.instruction(13, 8)
    io.regSelA := io.instruction(19, 14)
    io.regSelB := io.instruction(25, 20)

    flags(0) := io.aluCompOut0(0)
    flags(1) := io.aluCompOut0(1)
    flags(2) := io.aluCompOut0(2)
    flags(3) := io.aluCompOut0(3)
    flags(4) := io.aluCompOut0(4)

    io.regWriteData := io.aluOut

  }

  when(io.instruction(2, 0) === "b001".U) {
    io.programCounterJump := io.instruction(31, 16)
    switch(io.instruction(6, 0)) {
      /* JUMP INSTRUCTIONS  */

      // JMP EQUAL
      is("b001001".U) {
        io.jump := flags(1)
      }

      // JMP GREATER
      is("b001010".U) {
        io.jump := flags(2)
      }

      // JMP LESS THAN
      is("b001100".U) {
        io.jump := flags(0)
      }

      // JMP GREATER OR EQUAL
      is("b001011".U) {
        io.jump := flags(4)
      }

      // JMP LESS THAN OR EQUAL
      is("b001101".U) {
        io.jump := flags(3)
      }

      // JMP NOT EQUAL
      is("b001110".U) {
        io.jump := ~flags(1)
      }

      // JMP MF
      is("b001111".U) {
        io.jump := 1.U
      }
    }
  }

  // Same register selection for both rom and ram ops


  when(io.instruction(3, 0) === "b0001".U) {
    /* RAM LOAD / STORE OPS  */
    val regsel = io.instruction(9, 5)
    val address = io.instruction(31, 15)
    io.dataMemoryAddress := address

    switch(io.instruction(4).asBool()) {
      // LOAD
      is(0.B) {
        io.regWriteEnable := 1.B
        io.regWriteSel := regsel
        io.regWriteData := io.dataMemoryReadData
      }

      // STORE
      is(1.B) {
        io.regSelA := regsel
        io.dataMemoryWriteEnable := 1.B
        io.dataMemoryWriteData := io.regA
      }
    }
  }

  when(io.instruction(4, 0) === "b00001".U) {
    /* TEST / CMP */
    // Get first register (required for both)
    val regsel1 = io.instruction(12, 6)

    // Read from that
    io.regSelA := regsel1
    io.regWriteEnable := 0.B

    // First alu opt is first register
    io.aluInA := io.regA

    switch(io.instruction(5).asBool()) {
      // TEST reg
      is(0.B) {
        io.aluInB := io.regA
        flags(0) := io.aluCompOut0(0)
        flags(1) := io.aluCompOut0(1)
        flags(2) := io.aluCompOut0(2)
        flags(3) := io.aluCompOut0(3)
        flags(4) := io.aluCompOut0(4)
      }

      // CMP reg, reg
      is(1.B) {
        // Second register
        val regsel2 = io.instruction(18, 12)
        io.regSelB := regsel2
        io.aluInB := io.regB
        flags(0) := io.aluComp(0)
        flags(1) := io.aluComp(1)
        flags(2) := io.aluComp(2)
        flags(3) := io.aluComp(3)
        flags(4) := io.aluComp(4)
      }
    }
  }

}