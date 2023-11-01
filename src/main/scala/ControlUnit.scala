import chisel3._
import chisel3.util.{is, _}

class ControlUnit(registerFile: RegisterFile, alu: ALU, dataMemory: DataMemory) extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))

    // Interface with program counter
    val stop = Output(Bool())
    val jump = Output(Bool())
    val run = Output(Bool())
    val programCounterJump = Input(UInt(16.W))
  })

  // FLAGS
  // 0: LESSER
  // 1: EQUAL
  // 2: GREATER
  // 3: LESS OR EQUAL
  // 4: GREATER OR EQUAL
  // 5:
  private var flags = RegInit(Vec(16, Bool()));

  // SET instruction
  // 1dddddxxxxxxxxxxxxxxxxxxxxxxxxxx    set register = d, value = x
  when(io.instruction(0) === 1.U) {
    registerFile.io.in_writeEnable := 1.B
    registerFile.io.in_writeSel := io.instruction(5, 0)
    registerFile.io.in_writeData := io.instruction(31, 6)
  }

  when(io.instruction(1, 0) === "b01".U) {
    /* ALU INSTRUCTIONS */
    alu.io.in_sel := io.instruction(7, 2)
    registerFile.io.in_writeSel := io.instruction(13, 8)
    registerFile.io.in_aSel := io.instruction(19, 14)
    registerFile.io.in_bSel := io.instruction(25, 20)

    flags(0) := alu.io.out_result_comp_0(0)
    flags(1) := alu.io.out_result_comp_0(1)
    flags(2) := alu.io.out_result_comp_0(2)
    flags(3) := alu.io.out_result_comp_0(3)
    flags(4) := alu.io.out_result_comp_0(4)

    registerFile.io.in_writeData := alu.io.out_result

  }

  when(io.instruction(2, 0) === "b001".U) {
    switch(io.instruction(6, 0)) {

      /* JUMP INSTRUCTIONS  */

      // JMP EQUAL
      is("b001001".U) {

      }

      // JMP GREATER
      is("b001010".U) {

      }

      // JMP LESS THAN
      is("b001100".U) {

      }

      // JMP GREATER OR EQUAL
      is("b001011".U) {

      }

      // JMP LESS THAN OR EQUAL
      is("b001101".U) {

      }

      // JMP NOT EQUAL
      is("b001110".U) {

      }

      // JMP MF
      is("b001111".U) {

      }
    }
  }


  when(io.instruction(3, 0) === "b0001".U) {
    /* RAM LOAD / STORE OPS  */

    switch (io.instruction(4)) {
      // LOAD
      is (0.U) {

      }

      // STORE
      is (1.U) {

      }
    }
  }

  when(io.instruction(4, 0) === "b00001".U) {
    /* ROM LOAD */
  }

  when(io.instruction(5, 0) === "b000001".U) {
    /* TEST / CMP */
    switch (io.instruction(6)) {
      // TEST reg reg
      is (0.U) {

      }

      // CMP reg
      is (1.U) {

      }
    }
  }

}