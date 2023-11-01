import chisel3._
import chisel3.util._

class ControlUnit extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))

    // kill
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
    // Debug
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

  // Always point register A and B from registerFile into ALU
  io.aluInA := io.regA
  io.aluInB := io.regB

  // SET instruction
  // 1dddddxxxxxxxxxxxxxxxxxxxxxxxxxx    set register = d, value = x
  when(io.instruction(31) === 1.U) {
    io.regWriteEnable := 1.B
    io.regWriteSel := io.instruction(30, 26)
    io.regWriteData := io.instruction(25, 0)
  }

  when(io.instruction(31, 30) === "b01".U) {
    /* ALU INSTRUCTIONS */
    io.aluSel := io.instruction(29, 24)
    io.regWriteSel := io.instruction(23, 19)

    io.regWriteEnable := 1.B
    io.regSelA := io.instruction(18, 14)
    io.regSelB := io.instruction(13, 9)

    flags(0) := io.aluCompOut0(0)
    flags(1) := io.aluCompOut0(1)
    flags(2) := io.aluCompOut0(2)
    flags(3) := io.aluCompOut0(3)
    flags(4) := io.aluCompOut0(4)

    io.regWriteData := io.aluOut

  }

  when(io.instruction(31, 29) === "b001".U) {
    val jump_kind = io.instruction(28, 26)
    val use_reg = io.instruction(25).asBool()

    io.regSelB := io.instruction(24, 20)
    io.programCounterJump := Mux(use_reg, io.regB, io.instruction(15, 0))

    switch(jump_kind) {
      /* JUMP INSTRUCTIONS  */

      // JMP EQUAL
      is("b001".U) {
        io.jump := flags(1)
      }

      // JMP GREATER
      is("b010".U) {
        io.jump := flags(2)
      }

      // JMP LESS THAN
      is("b100".U) {
        io.jump := flags(0)
      }

      // JMP GREATER OR EQUAL
      is("b011".U) {
        io.jump := flags(4)
      }

      // JMP LESS THAN OR EQUAL
      is("b101".U) {
        io.jump := flags(3)
      }

      // JMP NOT EQUAL
      is("b110".U) {
        io.jump := ~flags(1)
      }

      // JMP MF
      is("b111".U) {
        io.jump := 1.U
      }
    }
  }

  when(io.instruction(31, 28) === "b0001".U) {
    /* RAM LOAD / STORE OPS  */
    val regsel = io.instruction(26, 22)
    val address = io.instruction(15, 0)
    io.dataMemoryAddress := address

    switch(io.instruction(27).asBool()) {
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

  when(io.instruction(31, 27) === "b00001".U) {
    /* TEST / CMP */
    // Get first register (required for both)
    val regsel1 = io.instruction(25, 21)

    // Read from that
    io.regSelA := regsel1
    io.regWriteEnable := 0.B

    switch(io.instruction(26).asBool()) {
      // TEST reg
      is(0.B) {
        // Use OR as we want self as output
        io.aluSel := 2.U
        io.regSelB := regsel1
        flags(0) := io.aluCompOut0(0)
        flags(1) := io.aluCompOut0(1)
        flags(2) := io.aluCompOut0(2)
        flags(3) := io.aluCompOut0(3)
        flags(4) := io.aluCompOut0(4)
      }

      // CMP reg, reg
      is(1.B) {
        // Second register
        io.regSelB := io.instruction(20, 16)
        flags(0) := io.aluComp(0)
        flags(1) := io.aluComp(1)
        flags(2) := io.aluComp(2)
        flags(3) := io.aluComp(3)
        flags(4) := io.aluComp(4)
      }
    }
  }

  // HALT
  when (io.instruction === "b00000000000000000000000000000001".U) {
    io.stop := 1.B
    io.done := 1.B
  }
}