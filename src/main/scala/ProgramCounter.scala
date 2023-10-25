import chisel3._
import chisel3.util._

class ProgramCounter extends Module {
  val io = IO(new Bundle {
    val stop = Input(Bool())
    val jump = Input(Bool())
    val run = Input(Bool())
    val programCounterJump = Input(UInt(16.W))
    val programCounter = Output(UInt(16.W))
  })

  private var pcReg: UInt = RegInit(0.U(16.W));

  when(!io.stop && io.run && io.jump) {
    pcReg := io.programCounterJump;
  }

  when(!io.stop && !io.jump && io.run) {
    pcReg := pcReg + 1.U(16.W)
  }

  io.programCounter := pcReg
}