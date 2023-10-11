import chisel3._
import chisel3.iotesters.PeekPokeTester

class RegisterFileTester(rf: RegisterFile) extends PeekPokeTester(rf) {
  poke(rf.io.in_aSel, 7)

  expect(peek(rf.io.in_aSel) == 7, "Wire set first")

  step(1);

  poke(rf.io.in_aSel, 0)
  poke(rf.io.in_writeSel, 0)
  poke(rf.io.in_writeData, 69)
  poke(rf.io.in_writeEnable, true)

  expect(peek(rf.io.out_a) == 0, "Updated after step")

  step(1)

  expect(peek(rf.io.out_a) == 69, "Updated after step")
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
