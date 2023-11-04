// Alternative to simulate circuit without using Scala
// SBT is quite slow, so this speeds things up a lot!

module CPUTop_tb;
    `ifndef STEP_MAX
        `define STEP_MAX 20000  // Default value if not passed from command line
    `endif

    integer outfile; // File handle for output file

    // Tick
    parameter TH = 5;
    parameter T  = TH * 2;

    reg [15:0] i;  // 16-bit loop counter

    // Declare the signals
    reg clock;
    reg reset;
    reg done_detected = 0;  // Flag to indicate if done is detected
    wire io_done;
    reg io_run;
    reg io_testerDataMemEnable;
    reg [15:0] io_testerDataMemAddress;
    wire [31:0] io_testerDataMemDataRead;
    reg io_testerDataMemWriteEnable;
    reg [31:0] io_testerDataMemDataWrite;
    reg io_testerProgMemEnable;
    reg [15:0] io_testerProgMemAddress;
    wire [31:0] io_testerProgMemDataRead;
    reg io_testerProgMemWriteEnable;
    reg [31:0] io_testerProgMemDataWrite;

    // Instantiate CPUTop
    CPUTop uut (
        .clock(clock),
        .reset(reset),
        .io_done(io_done),
        .io_run(io_run),
        .io_testerDataMemEnable(io_testerDataMemEnable),
        .io_testerDataMemAddress(io_testerDataMemAddress),
        .io_testerDataMemDataRead(io_testerDataMemDataRead),
        .io_testerDataMemWriteEnable(io_testerDataMemWriteEnable),
        .io_testerDataMemDataWrite(io_testerDataMemDataWrite),
        .io_testerProgMemEnable(io_testerProgMemEnable),
        .io_testerProgMemAddress(io_testerProgMemAddress),
        .io_testerProgMemDataRead(io_testerProgMemDataRead),
        .io_testerProgMemWriteEnable(io_testerProgMemWriteEnable),
        .io_testerProgMemDataWrite(io_testerProgMemDataWrite)
    );

    initial begin
        // Dump if flag is set
        `ifdef VCD
            $dumpfile("cpu_sim.vcd");
            $dumpvars(0, CPUTop_tb);
        `endif

        // Initialize signals
        clock = 0;
        reset = 1;
        #T reset = 0;
        
        $readmemh("out.hex", uut.programMemory.memory);
        $readmemh("mem.hex", uut.dataMemory.memory);

        $display("Starting test...");

        // Disable test signals
        io_testerDataMemEnable = 0;
        io_testerDataMemWriteEnable = 0;
        io_testerProgMemEnable = 0;
        io_testerProgMemWriteEnable = 0;

        // Start the test
        i = 0;
        io_run = 1;
        
        // Wait for the test to finish
        wait_for_done_or_max();

        $writememh("mem_out.hex", uut.dataMemory.memory);

        // Optionally, finish the test
        $finish;
    end

    task wait_for_done_or_max;
        if (!done_detected && i < `STEP_MAX) begin
            #T;
            i = i + 1;
            wait_for_done_or_max;
        end
    endtask

    always @(posedge io_done or negedge done_detected) begin
        if (!done_detected) 
            done_detected = 1;
    end


    // Clock generation
    always #TH clock = ~clock;

endmodule
