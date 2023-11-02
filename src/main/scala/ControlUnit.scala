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
    val aluSigned = Output(Bool())
    val aluInA = Output(UInt(32.W))
    val aluInB = Output(UInt(32.W))
    val aluInASigned = Output(SInt(32.W))
    val aluInBSigned = Output(SInt(32.W))
    val aluOut = Input(UInt(32.W))
    val aluOutSigned = Input(SInt(32.W))
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
  io.aluSigned := 0.B

  // Always point register A and B from registerFile into ALU
  io.aluInA := io.regA
  io.aluInB := io.regB
  io.aluInASigned := io.regA.asSInt()
  io.aluInBSigned := io.regB.asSInt()

  // SET instruction
  // 1dddddxxxxxxxxxxxxxxxxxxxxxxxxxx    set register = d, value = x
  when(io.instruction(31) === 1.U) {
    val dest = io.instruction(30, 26)
    val immediate = io.instruction(25, 0)

    io.regWriteEnable := 1.B
    io.regWriteSel := dest
    io.regWriteData := immediate
  }

  when(io.instruction(31, 30) === "b01".U) {
    /* ALU INSTRUCTIONS */

    val op_type = io.instruction(29, 26)
    val is_signed = io.instruction(25).asBool()
    val load_immediate = io.instruction(24).asBool()
    val dest = io.instruction(23, 19)
    val source1 = io.instruction(18, 14)
    val source2 = io.instruction(13, 9)
    val immediate = io.instruction(13, 0)

    io.aluSigned := is_signed
    io.aluSel := op_type
    io.regWriteSel := dest

    io.regWriteEnable := 1.B
    io.regSelA := source1
    io.regSelB := source2

    when(load_immediate) {
      io.regSelB := 0.U
      io.aluInB := immediate
      io.aluInBSigned := immediate.asSInt()
    }

    flags(0) := io.aluCompOut0(0)
    flags(1) := io.aluCompOut0(1)
    flags(2) := io.aluCompOut0(2)
    flags(3) := io.aluCompOut0(3)
    flags(4) := io.aluCompOut0(4)

    io.regWriteData := Mux(is_signed, io.aluOutSigned.asUInt(), io.aluOut)
  }

  when(io.instruction(31, 29) === "b001".U) {
    val jump_kind = io.instruction(28, 26)
    val use_reg = io.instruction(25).asBool()
    val source1 = io.instruction(24, 20)
    val address = io.instruction(15, 0)

    io.regSelB := source1
    io.programCounterJump := Mux(use_reg, io.regB, address)

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
    /* RAM LOAD / STORE OPS */
    val op_type = io.instruction(27).asBool()
    val use_reg = io.instruction(26).asBool()
    // Load into, or store from.
    val source1 = io.instruction(25, 21)

    // Read or write to address or address at reg location
    io.regSelB := io.instruction(20, 16)
    val address = io.instruction(15, 0)

    // If we use reg, read the address we load or store from/to
    // from the second register provided
    io.dataMemoryAddress := Mux(use_reg, io.regB, address)

    switch(op_type) {
      // LOAD
      is(0.B) {
        io.regWriteEnable := 1.B
        io.regWriteSel := source1
        io.regWriteData := io.dataMemoryReadData
      }

      // STORE
      is(1.B) {
        io.regSelA := source1
        io.dataMemoryWriteEnable := 1.B
        io.dataMemoryWriteData := io.regA
      }
    }
  }

  when(io.instruction(31, 27) === "b00001".U) {
    /* TEST / CMP */
    // Get first register (required for both)
    val op_type = io.instruction(26).asBool()
    val is_signed = io.instruction(25)
    val load_immediate = io.instruction(24).asBool()
    val source1 = io.instruction(23, 19)
    val source2 = io.instruction(18, 14)
    val immediate = io.instruction(18, 0)

    io.aluSigned := is_signed
    io.regSelA := source1
    io.regWriteEnable := 0.B

    switch(op_type) {
      // TEST reg
      is(0.B) {
        // Use OR as we want self as output
        io.aluSel := 2.U
        io.regSelB := source1
        flags(0) := io.aluCompOut0(0)
        flags(1) := io.aluCompOut0(1)
        flags(2) := io.aluCompOut0(2)
        flags(3) := io.aluCompOut0(3)
        flags(4) := io.aluCompOut0(4)
      }

      // CMP reg, reg
      is(1.B) {
        // Second register
        io.regSelB := source2

        when(load_immediate) {
          io.aluInB := immediate
          io.aluInBSigned := immediate.asSInt()
        }

        flags(0) := io.aluComp(0)
        flags(1) := io.aluComp(1)
        flags(2) := io.aluComp(2)
        flags(3) := io.aluComp(3)
        flags(4) := io.aluComp(4)
      }
    }
  }

  // HALT
  when(io.instruction === "b00000000000000000000000000000001".U) {
    io.stop := 1.B
    io.done := 1.B
  }
}