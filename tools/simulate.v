// Alternative to simulate circuit without using Scala
// SBT is quite slow, so this speeds things up a lot!

module CPUTop_tb;


    `ifndef STEP_MAX
        `define STEP_MAX 20000  // Default value if not passed from command line
    `endif

    `ifndef PROG_MAX
        `define PROG_MAX 65535  // Default value if not passed from command line
    `endif

    `ifndef DATA_MAX
        `define DATA_MAX 65535  // Default value if not passed from command line
    `endif

    `ifndef DATA_READ_START
        `define DATA_READ_START 0
    `endif

    `ifndef DATA_READ_END
        `define DATA_READ_END 9
    `endif

    integer outfile; // File handle for output file

    // Tick
    parameter TH = 5;
    parameter T  = TH * 2;

    reg [15:0] i;  // 16-bit loop counter

    // Declare input memory
    reg [31:0] prog_mem [0:`PROG_MAX];
    reg [31:0] data_mem [0:`DATA_MAX];

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
        
        $readmemh("out.hex", prog_mem);
        $readmemh("mem.hex", data_mem);

        // Load data into Program Memory similarly...
        io_testerProgMemEnable = 1;
        io_testerProgMemWriteEnable = 1;
        
        // Loop over the program memory
        for (i = 0; i < `PROG_MAX; i = i + 1) begin
            // Print i
            io_testerProgMemAddress = i;
            io_testerProgMemDataWrite = prog_mem[i];
            #T;
        end

        // Load data into Data Memory
        io_testerDataMemEnable = 1;
        io_testerDataMemWriteEnable = 1;

        // Loop over the data memory
        for (i = 0; i < `DATA_MAX; i = i + 1) begin
            // Print i
            io_testerDataMemAddress = i;
            io_testerDataMemDataWrite = data_mem[i];
            #T;
        end

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

        // Read the data memory
        io_testerDataMemEnable = 1;
        outfile = $fopen("mem_out.hex", "w");

        for (i = `DATA_READ_START; i < `DATA_READ_END; i = i + 1) begin
            io_testerDataMemAddress = i;
            #T;
            $fwrite(outfile, "%h\n", io_testerDataMemDataRead);
        end

        $fclose(outfile);

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
