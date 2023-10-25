import chisel3._
import chisel3.iotesters.PeekPokeTester

class ALUTester(alu: ALU) extends PeekPokeTester(alu) {
  poke(alu.io.in_sel, 0)
  poke(alu.io.in_op1, 1)
  poke(alu.io.in_op2, 0)

  step(1)

  expect(peek(alu.io.out_result) == 4294967294L, "Compare integer output")
}

object ALUTester {
  def main(args: Array[String]): Unit = {
    (new chisel3.stage.ChiselStage).emitVerilog(new ALU, args)

    println("Testing ALU")
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on",
        "--target-dir", "generated",
        "--top-name", "RegisterFile"),
      () => new ALU()) {
      c => new ALUTester(c)
    }
  }
}
