import chisel3._
import chisel3.iotesters.PeekPokeTester

class CPUTester(dut: ControlUnit) extends PeekPokeTester(dut) {
  poke(dut.io.instruction, 2483028068L);
  step(1)

  System.out.println("AYO: "+peek(dut.io.debugOutput))
}


object CPUTester {
  def main(args: Array[String]): Unit = {
    println("Testing CPU")
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on",
        "--target-dir", "generated",
        "--top-name", "ActualCPUTester"),
      () => new ControlUnit()) {
      c => new CPUTester(c)
    }
  }
}
