import chisel3._
import chisel3.util._

class RegisterFile extends Module {
  val io = IO(new Bundle {
    //Define the module interface here (inputs/outputs)
    val in_aSel = Input(UInt(5.W))
    val in_bSel = Input(UInt(5.W))
    val in_writeData = Input(UInt(32.W))
    val in_writeSel = Input(UInt(5.W))
    val in_writeEnable = Input(Bool())

    val out_a = Output(UInt(32.W))
    val out_b = Output(UInt(32.W))
  })

  val regfile = RegInit(VecInit(Seq.fill(32)(0.U(32.W))));

  io.out_a := regfile(io.in_aSel);
  io.out_b := regfile(io.in_bSel);

  regfile(io.in_writeSel) := Mux(io.in_writeEnable, io.in_writeData, regfile(io.in_writeSel))
}
