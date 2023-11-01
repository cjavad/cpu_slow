import chisel3._
import chisel3.iotesters.PeekPokeTester

class ActualCPUTester(dut: CPUTop) extends PeekPokeTester(dut) {
  val program = Array(
    1006632964L,
    3154116677L,
    2147483648L,
    1006632965L,
    2147483658L,
    2214592522L,
    201392128L,
    603979777L,
  )

  System.out.print("\nLoading the program memory with instructions... ")

  for (address <- 0 to program.length - 1) {
    poke(dut.io.testerProgMemEnable, 1)
    poke(dut.io.testerProgMemWriteEnable, 1)
    poke(dut.io.testerProgMemAddress, address)
    poke(dut.io.testerProgMemDataWrite, program(address))
    step(1)
  }
  System.out.println("Done!")

  poke(dut.io.testerProgMemEnable, 0)
  poke(dut.io.run, 1);

  step(100)
}


object ActualCPUTester {
  def main(args: Array[String]): Unit = {
    println("Testing CPU")
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on",
        "--target-dir", "generated",
        "--top-name", "ActualCPUTester"),
      () => new CPUTop()) {
      c => new ActualCPUTester(c)
    }
  }
}
