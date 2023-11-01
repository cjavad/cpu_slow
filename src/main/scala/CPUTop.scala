import chisel3._
import chisel3.util._

class CPUTop extends Module {
  val io = IO(new Bundle {
    val done = Output(Bool ())
    val run = Input(Bool ())
    //This signals are used by the tester for loading and dumping the memory content, do not touch
    val testerDataMemEnable = Input(Bool ())
    val testerDataMemAddress = Input(UInt (16.W))
    val testerDataMemDataRead = Output(UInt (32.W))
    val testerDataMemWriteEnable = Input(Bool ())
    val testerDataMemDataWrite = Input(UInt (32.W))
    //This signals are used by the tester for loading and dumping the memory content, do not touch
    val testerProgMemEnable = Input(Bool ())
    val testerProgMemAddress = Input(UInt (16.W))
    val testerProgMemDataRead = Output(UInt (32.W))
    val testerProgMemWriteEnable = Input(Bool ())
    val testerProgMemDataWrite = Input(UInt (32.W))
  })

  //Creating components
  val programCounter = Module(new ProgramCounter())
  val dataMemory = Module(new DataMemory())
  val programMemory = Module(new ProgramMemory())
  val alu = Module(new ALU())
  val registerFile = Module(new RegisterFile())
  val controlUnit = Module(new ControlUnit())

  //Connecting the modules
  programCounter.io.run := io.run
  programMemory.io.address := programCounter.io.programCounter

  ////////////////////////////////////////////
  //Continue here with your connections
  ////////////////////////////////////////////
  io.done := controlUnit.io.done
  controlUnit.io.instruction := programMemory.io.instructionRead

  // Program counter interface
  programCounter.io.stop := controlUnit.io.stop
  programCounter.io.jump := controlUnit.io.jump
  programCounter.io.programCounterJump := controlUnit.io.programCounterJump

  // Interface with dataMemory
  controlUnit.io.dataMemoryReadData := dataMemory.io.dataRead
  dataMemory.io.address := controlUnit.io.dataMemoryAddress
  dataMemory.io.dataWrite := controlUnit.io.dataMemoryWriteData
  dataMemory.io.writeEnable := controlUnit.io.dataMemoryWriteEnable

  // Interface with registerFile
  controlUnit.io.regA := registerFile.io.out_a
  controlUnit.io.regB := registerFile.io.out_b
  registerFile.io.in_aSel := controlUnit.io.regSelA
  registerFile.io.in_bSel := controlUnit.io.regSelB
  registerFile.io.in_writeSel := controlUnit.io.regWriteSel
  registerFile.io.in_writeData := controlUnit.io.regWriteData
  registerFile.io.in_writeEnable := controlUnit.io.regWriteEnable

  // Interface with ALU
  alu.io.in_sel := controlUnit.io.aluSel
  alu.io.in_op1 := controlUnit.io.aluInA
  alu.io.in_op2 := controlUnit.io.aluInB

  controlUnit.io.aluOut := alu.io.out_result
  controlUnit.io.aluComp := alu.io.out_comp
  controlUnit.io.aluCompOut0 := alu.io.out_result_comp_0


  //This signals are used by the tester for loading the program to the program memory, do not touch
  programMemory.io.testerAddress := io.testerProgMemAddress
  io.testerProgMemDataRead := programMemory.io.testerDataRead
  programMemory.io.testerDataWrite := io.testerProgMemDataWrite
  programMemory.io.testerEnable := io.testerProgMemEnable
  programMemory.io.testerWriteEnable := io.testerProgMemWriteEnable
  //This signals are used by the tester for loading and dumping the data memory content, do not touch
  dataMemory.io.testerAddress := io.testerDataMemAddress
  io.testerDataMemDataRead := dataMemory.io.testerDataRead
  dataMemory.io.testerDataWrite := io.testerDataMemDataWrite
  dataMemory.io.testerEnable := io.testerDataMemEnable
  dataMemory.io.testerWriteEnable := io.testerDataMemWriteEnable
}