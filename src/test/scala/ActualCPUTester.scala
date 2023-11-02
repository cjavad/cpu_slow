import chisel3._
import chisel3.iotesters.PeekPokeTester

class ActualCPUTester(dut: CPUTop) extends PeekPokeTester(dut) {
  val program = Array(
    2214592513L,
    1359495177L,
    404750336L,
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

  while (0 == peek(dut.io.done) && steps <= maxSteps) {
    steps += 1
    step(1)
  }

  System.out.println("Stepped: " + steps + " times before halting or reaching beyond last instruction")

  //Dump the data memory content
  System.out.print("\nDump the data memory content... ")

  val size = 100;

  val memory_buffer = new Array[Int](size + 1)
  for (i <- 0 to size) { //Location of the original image
    poke(dut.io.testerDataMemEnable, 1)
    poke(dut.io.testerDataMemWriteEnable, 0)
    poke(dut.io.testerDataMemAddress, i)
    val data = peek(dut.io.testerDataMemDataRead)
    memory_buffer(i) = data.toInt
    step(1)
  }
  poke(dut.io.testerDataMemEnable, 0)
  System.out.println("Done! Dumped " + memory_buffer.length + " words of data memory")

  // Output as hex
  for (i <- 0 to size) {

    if (i % 8 == 0) {
      print("\n" + i.toHexString + ": ")
    }
    print(memory_buffer(i).toHexString + " ")
  }
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
