import chisel3._
import chisel3.iotesters.PeekPokeTester

class ActualCPUTester(dut: CPUTop) extends PeekPokeTester(dut) {
  val program = Array(
    2147483658L,
    402653199L,
    2214592527L,
    339804160L,
    1L,
  )

  System.out.print("\nLoading the program memory with instructions... ")

  for (address <- program.indices) {
    poke(dut.io.testerProgMemEnable, 1)
    poke(dut.io.testerProgMemWriteEnable, 1)
    poke(dut.io.testerProgMemAddress, address)
    poke(dut.io.testerProgMemDataWrite, program(address))
    step(1)
  }
  System.out.println("Done!")

  poke(dut.io.testerProgMemEnable, 0)
  poke(dut.io.run, 1)

  var steps = 0
  val maxSteps = 20000

  while (0 == peek(dut.io.done)) {
    steps += 1
    step(1)
  }

  System.out.println("Stepped: " + steps + " times before halting or reaching beyond last instruction")
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
