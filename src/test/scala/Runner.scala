import chisel3._
import chisel3.iotesters.PeekPokeTester
import java.nio.file.{Files, Paths}


class Runner(program: Array[Long], memory: Array[Long], dut: CPUTop) extends PeekPokeTester(dut) {

  System.out.print("\nLoading program and memory... ")

  for (address <- program.indices) {
    poke(dut.io.testerProgMemEnable, 1)
    poke(dut.io.testerProgMemWriteEnable, 1)
    poke(dut.io.testerProgMemAddress, address)
    poke(dut.io.testerProgMemDataWrite, program(address))
    step(1)
  }

  poke(dut.io.testerProgMemEnable, 0)

  for (address <- memory.indices) {
    poke(dut.io.testerDataMemEnable, 1)
    poke(dut.io.testerDataMemWriteEnable, 1)
    poke(dut.io.testerDataMemAddress, address)
    poke(dut.io.testerDataMemDataWrite, memory(address))
    step(1)
  }

  poke(dut.io.testerDataMemEnable, 0)

  System.out.println("Done!")

  poke(dut.io.run, 1)

  var steps = 0
  val maxSteps = 20000

  while (0 == peek(dut.io.done) && steps <= maxSteps) {
    steps += 1
    step(1)
  }

  System.out.println("Stepped: " + steps + " times before halting")

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


object Runner {
  def main(args: Array[String]): Unit = {
    // Parse args to generate program and memory
    if (args.length < 2) {
      println("Usage: sbt \"runMain Runner <path to program> <path to memory?>\"")
      return
    }


    // Load program as every 32 bits (binary file output not seperated by bytes)
    def readFileAsUnsignedInts(filename: String): Array[Long] = {
      val byteArray = Files.readAllBytes(Paths.get(filename))
      byteArray.grouped(4).map { group =>
        group.foldLeft(0L)((acc, byte) => (acc << 8) | (byte & 0xFFL))
      }.toArray
    }

    val program = readFileAsUnsignedInts(args(0))
    val memory = readFileAsUnsignedInts(args(1))

    println("Program: " + program.mkString(" "))

    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on",
        "--target-dir", "generated",
        "--top-name", "Runner"),
      () => new CPUTop()) {
      c => new Runner(program, memory, c)
    }
  }
}
