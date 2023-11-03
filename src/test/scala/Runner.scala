import chisel3._
import chisel3.iotesters.PeekPokeTester
import java.nio.file.{Files, Paths}


class Runner(
  program: Array[Long],
  memory: Array[Long],
  outputSectionStart: Long,
  outputSectionEnd: Long, 
  dut: CPUTop  
) extends PeekPokeTester(dut) {

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

  val size = outputSectionEnd - outputSectionStart;

  val memory_buffer = new Array[Int](size + 1)

  for (i <- outputSectionStart to outputSectionEnd) {
    if (i % 8 == 0) {
      print("\n" + i.toHexString + ": ")
    }

    poke(dut.io.testerDataMemEnable, 1)
    poke(dut.io.testerDataMemWriteEnable, 0)
    poke(dut.io.testerDataMemAddress, i)
    val data = peek(dut.io.testerDataMemDataRead)
    memory_buffer(i) = data.toInt
    print(memory_buffer(i).toHexString + " ")
    step(1)
  }

  poke(dut.io.testerDataMemEnable, 0)

  // Dump memory as base64
  val base64 = java.util.Base64.getEncoder.encodeToString(memory_buffer.map(_.toByte))
  System.out.println("\n\nBase64 encoded memory: " + base64)
}

object Runner {
  def main(args: Array[String]): Unit = {
    // Parse args to generate program and memory
    if (args.length < 2) {
      println("Usage: sbt \"test:run <path to program> <path to memory?>\"")
      println("Or   : sbt \"test:run <base64 encoded program> <base64 encoded memory?>\"")
      return
    }

    def asUnsignedInts(bytes: Array[Byte]): Array[Long] = {
      bytes.grouped(4).map { group =>
        group.foldLeft(0L)((acc, byte) => (acc << 8) | (byte & 0xFFL))
      }.toArray
    }

    // Load program as every 32 bits (binary file output not seperated by bytes)
    def readFileAsUnsignedInts(filename: String): Array[Long] = {
      asUnsignedInts(Files.readAllBytes(Paths.get(filename)))
    }

    def convertBase64ToUnsignedInts(base64: String): Array[Long] = {
      asUnsignedInts(java.util.Base64.getDecoder.decode(base64))
    }

    // Check if file exists
    val isBase64 = !args(0).contains(".")

    val program = if (isBase64) {
      convertBase64ToUnsignedInts(args(0))
    } else {
      readFileAsUnsignedInts(args(0))
    }

    val memory = if (args.length > 1) {
      if (isBase64) {
        convertBase64ToUnsignedInts(args(1))
      } else {
        readFileAsUnsignedInts(args(1))
      }
    } else {
      Array.fill(10)(0L)
    }


    iotesters.Driver.execute(
      Array(),
      () => new CPUTop()) {
      c => new Runner(
        program,
        memory,
        0,
        100, 
        c)
    }
  }
}
