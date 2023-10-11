import chisel3._
import chisel3.iotesters.PeekPokeTester

class RegisterFileTester(dut: RegisterFile) extends PeekPokeTester(dut) {
  poke(dut.io.in_aSel, 7)

  expect(peek(dut.io.in_aSel) == 7, "Wire set first")

  step(1);

  poke(dut.io.in_aSel, 0)
  poke(dut.io.in_writeSel, 0)
  poke(dut.io.in_writeData, 69)
  poke(dut.io.in_writeEnable, true)

  expect(peek(dut.io.out_a) == 0, "Updated after step")

  step(1)

  expect(peek(dut.io.out_a) == 69, "Updated after step")
}

object RegisterFileTester {
  def main(args: Array[String]): Unit = {
    println("Testing Register File")
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on",
        "--target-dir", "generated",
        "--top-name", "RegisterFile"),
      () => new RegisterFile()) {
      c => new RegisterFileTester(c)
    }
  }
}
