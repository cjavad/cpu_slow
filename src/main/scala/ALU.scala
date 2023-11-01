import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val in_sel = Input(UInt(5.W))

    val in_op1 = Input(UInt(32.W))
    val in_op2 = Input(UInt(32.W))

    val out_result = Output(UInt(32.W))

    // < = > <= >=
    val out_comp = Output(Vec(5, Bool()))
    val out_result_comp_0 = Output(Vec(5, Bool()))
  })

  private val is_greater = io.in_op1 > io.in_op2
  private val is_lesser = io.in_op1 < io.in_op2
  private val is_equal =  ~(is_lesser | is_greater)

  io.out_comp(0) := is_lesser
  io.out_comp(1) := is_equal
  io.out_comp(2) := is_greater
  io.out_comp(3) := is_lesser | is_equal
  io.out_comp(4) := is_greater | is_equal

  io.out_result := 0.U // Default value

  //Implement this module here
  switch (io.in_sel) {
    is (0.U) {
      io.out_result := ~io.in_op1
    }
    is (1.U) {
      io.out_result := io.in_op1 & io.in_op2
    }
    is (2.U) {
      io.out_result := io.in_op1 | io.in_op2
    }
    is (3.U) {
      io.out_result := io.in_op1 ^ io.in_op2

    }
    is (4.U) {
      io.out_result := io.in_op1 + io.in_op2
    }
    is (5.U) {
      io.out_result := io.in_op1 - io.in_op2
    }
    is (6.U) {
      io.out_result := io.in_op1 / io.in_op2
    }
    is (7.U) {
      io.out_result := io.in_op1 % io.in_op2
    }
    is (8.U) {
      io.out_result := io.in_op1 * io.in_op2

    }
    is (9.U) {
      io.out_result := io.in_op1 >> io.in_op2(5, 0)
    }
    is (10.U) {
      io.out_result := io.in_op1 << io.in_op2(5, 0)
    }

    is (11.U) {}
    is (12.U) {}
    is (13.U) {}
    is (14.U) {}
    is (15.U) {}
  }

  private val is_lesser_0 = io.out_result < 0.U
  private val is_greater_0 = io.out_result > 0.U
  private val is_equal_0 = ~(is_lesser_0 | is_greater_0)

  io.out_result_comp_0(0) := is_lesser_0
  io.out_result_comp_0(1) := is_equal_0
  io.out_result_comp_0(2) := is_greater_0
  io.out_result_comp_0(3) := is_lesser_0 | is_equal_0
  io.out_result_comp_0(4) := is_greater_0 | is_equal_0
}