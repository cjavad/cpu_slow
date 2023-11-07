// Alternative to simulate circuit without using Scala
// SBT is quite slow, so this speeds things up a lot!
`timescale 1ns/1ns

module CPUTop_tb;
    `ifndef STEP_MAX
        `define STEP_MAX 100000  // Default value if not passed from command line
    `endif

    `ifndef RING_0
        // The last address which is considered privileged
        // Used to detect rougue jumps to privileged addresses
        // As it is only allowed to jump to RING_0 by jumping
        // to 0.
        `define RING_0 65535
    `endif

    // Valid jump address from unprivileged to privileged
    `ifndef RING_0_ENTRYPOINT
        `define RING_0_ENTRYPOINT 0
    `endif

    `ifndef RING_0_MEMORY
        // Where the privileged memory ends (always starts at 0)
        `define RING_0_MEMORY 0
    `endif

    `ifndef RING_0_REGISTERS
        // Where the privileged registers end (always starts at 0)
        // Fx. first 4 registers are privileged
        `define RING_0_REGISTERS 0
    `endif

    // Tick
    parameter TH = 1;
    parameter T  = TH * 2;

    reg [31:0] i;  // 32-bit loop counter

    // Control signals
    reg  clock;
    reg  reset;
    reg  tester;
    reg  io_run;
    wire io_done;

    reg privileged;
    reg do_syscall;

    reg [15:0] programMemoryOffset;
    reg [15:0] dataMemoryOffset;

    // Instantiate CPUTop
    CPUTop uut (
        .clock(clock),
        .reset(reset),
        .io_done(io_done),
        .io_run(io_run),
        .io_testerDataMemEnable(tester),
        .io_testerProgMemEnable(tester),
        .io_dataMemoryOffset(dataMemoryOffset),
        .io_programMemoryOffset(programMemoryOffset)
    );


    initial begin
        $dumpfile("debug.vcd");
        $dumpvars(0, CPUTop_tb);
 
        $readmemh("in.hex", uut.programMemory.memory);
        $readmemh("mem.hex", uut.dataMemory.memory);

        // Initialize signals
        tester = 0;
        clock = 0;
        reset = 1;

        privileged = 0;
        do_syscall = 0;
        dataMemoryOffset = 0;
        programMemoryOffset = 0;

        i = 0;           // Initialize counter
        #T reset = 0;    // Release reset after defined time T
        io_run = 1;      // Start CPU operation
    end

    always @(posedge clock) begin
        // print pc
        // $display("PC: %d", uut.programCounter.io_programCounter);

        // Increment the counter each clock cycle
        i = i + 1;

        if (io_done || (i >= `STEP_MAX)) begin
            end_simulation;
        end
    end


    // Check if the program counter is in privileged mode
    always @(uut.programCounter.io_programCounter) begin
        privileged = (uut.programCounter.io_programCounter <= `RING_0);
    end

    always @(uut.registerFile.regfile_31) begin 
        do_syscall = (uut.registerFile.regfile_31 > 0);
    end

    // When jump goes high check if the jump is valid
    // But we still have to check every clock cycle
    // As we can jump back to back.
    always @(posedge clock or posedge uut.programCounter.io_jump) begin        
        if (!privileged && !do_syscall) begin
            // Set jump address to relative virtual address
            programMemoryOffset = `RING_0 + 1;
        end

        if (!privileged && do_syscall) begin
            programMemoryOffset = 0;
        end

        if (privileged) begin
            programMemoryOffset = 0;
        end
        
        if (uut.programCounter.io_jump) begin
            // $display("Jump to %d from %d with offset %d with priv %d", uut.programCounter.io_programCounterJump, uut.programCounter.io_programCounter, programMemoryOffset, privileged);

            if (!privileged && uut.programCounter.io_programCounterJump <= `RING_0 && uut.programCounter.io_programCounterJump != `RING_0_ENTRYPOINT) begin
                $display("Jump to unprivileged address %d", uut.programCounter.io_programCounterJump);
                end_simulation;
            end
        end
    end

    // When we select an address from memory
    always @(uut.dataMemory.io_address) begin
        if (!privileged) begin
            // Offset the address to virtual address
            dataMemoryOffset = `RING_0 + 1;
        end

        else begin
            dataMemoryOffset = 0;
        end

        if (!privileged && uut.dataMemory.io_address <= `RING_0_MEMORY) begin
            $display("Unauthorized access to privileged memory %d", uut.dataMemory.io_address);
            end_simulation;
        end
    end

    // When we select a register
    always @(uut.registerFile.io_in_aSel) begin
        if (!privileged && uut.registerFile.io_in_aSel <= `RING_0_REGISTERS) begin
            $display("Unauthorized access to privileged selA register %d", uut.registerFile.io_in_aSel);
            end_simulation;
        end
    end

    always @(uut.registerFile.io_in_bSel) begin
        if (!privileged && uut.registerFile.io_in_bSel <= `RING_0_REGISTERS) begin
            $display("Unauthorized access to privileged selB register %d", uut.registerFile.io_in_bSel);
            end_simulation;
        end
    end

    always @(posedge clock) begin
        if (!privileged && uut.registerFile.io_in_writeEnable && uut.registerFile.io_in_writeSel <= `RING_0_REGISTERS) begin
            $display("Unauthorized access to privileged write register %d", uut.registerFile.io_in_writeSel);
            end_simulation;
        end
    end

    task end_simulation;
        begin
            $writememh("out.hex", uut.dataMemory.memory);
            $finish;
        end
    endtask

    // Clock generation
    always #TH clock = ~clock;
endmodule
