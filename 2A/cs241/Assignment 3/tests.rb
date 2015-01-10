@tests = Hash.new
@groups = Hash.new(Array.new)

def parse_xxd xxd_output
    temp_array = []
    xxd_output.split("\n").each do |line|
        temp = line.match(/((\d|[a-f]){4} )+/).to_s
        temp_array << temp[/.*(?= $)/]
    end
    temp_array.join("\n")
end

def bin_to_hex_string bin_string
    bin_num = bin_string.delete(" ").to_i(2)
    hex_string = "0x%08x" % bin_num
    hex_string[2..-1].insert(4," ")
end

def print_title title
    puts "\033[1;35m#{ title }\033[;0m"
end

def run_valgrind
    `valgrind ./asm < test.asm > valgrind_out`
    File.delete("valgrind_out")
end

def parse_test_string test_string, test_names
    tests_to_run = []
    tests = test_string.split(/ *, */)
    tests.each do |test_num|
        unless test_num =~ /^(\d| *- *)+$/
            if @groups.has_key? test_num.to_sym
                @groups[test_num.to_sym].each do |group_test|
                    tests_to_run << group_test
                end
            else
                puts "Invalid test number or range \"#{ test_num }\""
                exit
            end
        else
            if test_num =~ /-/
                range = test_num.split(/ *- */)
                range_start = range[0].to_i
                range_end = range[1].to_i
                if range_end > test_names.count
                    puts "#{ range_end } is greater than the number of tests"
                    exit
                end
                (range_start..range_end).each do |i|
                    i -= 1
                    tests_to_run << test_names[i]
                end
            else
                i = test_num.to_i - 1
                tests_to_run << test_names[i]
            end
        end
    end
    tests_to_run
end

def run_test( test_name, test_case )
    args = Hash.new
    test_case.call(args)

    print_title test_name

    input = args[:input]
    expected_output = args[:output]
    expected_err = args[:err]

    File.open( "test.asm", "w" ) { |file| file.puts input }

    success = 0

    raw_output = `java Asm < test.asm 2> err | xxd`
    output = parse_xxd raw_output

    temp = `cat err`
    err = temp.split("\n").sort.join("\n")

    print "Output: "
    if output  == expected_output
        puts "\033[0;32mPass\033[;0m"
        success += 1
    else
        puts "\033[0;31mFail\033[;0m"
        puts "Expected:\n#{ expected_output }"
        puts "Actual:\n#{ output }\n"
    end

    print "Error: "
    if (err =~ /ERROR/ && expected_err =~ /ERROR/) || err == expected_err
        puts "\033[0;32mPass\033[;0m"
        success += 1
    else
        puts "\033[0;31mFail\033[;0m"
        puts "Expected:\n#{ expected_err }"
        puts "Actual:\n#{ err }"
    end

    if @run_binasm
        `java cs241.binasm < test.asm > temp.mips 2> binasm_err`
        binasm = `cat binasm_err`

        print "\nBinasm: "
        if binasm =~ /ParseError/
            if err =~ /ERROR/
                puts "\033[0;32mPass\033[;0m"
                puts "Binasm error:\n#{ binasm }"
                puts "Asm error:\n#{ err }"
                success += 1
            else
                puts "\033[0;31mFail: no error deteched in asm\033[;0m"
                puts "Expected error:\n#{ binasm }"
            end
        else
            binasm = `cat temp.mips | xxd`
            if raw_output == binasm
                puts "\033[0;32mPass\033[;0m"
                success += 1
            else 
                puts "\033[0;31mFail\033[;0m"
                puts "Expected:\n#{ binasm }"
                puts "Actual:\n#{ raw_output }"
            end
        end

        File.delete("temp.mips")
        File.delete("binasm_err")
    end

    if @run_valgrind && ( (@run_binasm && success == 3) || success == 2 )
        run_valgrind
    end

    File.delete("err")
    File.delete("test.asm")
end

def test test_name, group=" ", &test_case
    @tests[test_name.to_sym] = test_case
    @groups[group.to_sym] += [test_name.to_sym]
end

test "Valid Store" do |args|

    args[:input] = <<-EOF
sw $31, 0($3)
    EOF

    # Expected values
    args[:output] = bin_to_hex_string "1010 1100 0111 1111 0000 0000 0000 0000"

    args[:err] = ""
end

test "Valid Brackets" do |args|

    args[:input] = <<-EOF
sw $31, 0$3
    EOF

    # Expected values
    args[:output] = ""

    args[:err] = "ERROR Invalid load word format"
end

@run_binasm = false
@run_valgrind = false
@run_tests = false
@test_string = ""
@format = false

test_names = @tests.keys.sort_by { |key| key.to_s }

help_text = <<-EOF
Assembler testing script
Created by Nik Klassen
Usage: ruby test.rb [ OPTIONS ]
Options:
   -b\t\tRun using cs241.binasm (must have sourced /u/cs241/setup for this to work)
   -v\t\tRun valgrind after each test - recommendation - use with only one or a few test cases at a time
   -h\t\tPrint this help message
   -a, --all\tRun all tests without prompt

When specifying tests, input a comma separated list of values or ranges.  i.e. 2, 3, 4 - 7
EOF

tests_to_run = []
ARGV.each do |arg|
    if arg == "-b"
        output = `touch binasm_test; java cs241.binasm < binasm_test 2>&1`
        `rm binasm_test`
        if output == "Error: Could not find or load main class cs241.binasm\n"
            puts "Error: cs241.binasm not found"
            exit
        else
            @run_binasm = true
        end
    elsif arg == "-v"
        valgrind = `which valgrind`
        if valgrind != ""
            @run_valgrind = true
        else
            puts "Error: Valgrind not found"
            exit
        end
    elsif arg == "-h" || arg == "--help"
        puts help_text
        exit
    elsif arg == "-a" || arg == "--all"
        @run_all = true
    elsif arg == "-t"
        @format = true
        ARGV.shift
        test_string = ARGV.shift
        tests_to_run = parse_test_string test_string, test_names
    else
        puts "Illegal option"
        puts help_text
        exit
    end
end

unless @format
    resp = ""
    if @run_all
        resp = "y"
    else
        print "Run all [Y/n]: "
        resp = gets.chomp
    end
end

if resp == "" || resp == "Y" || resp == "y"
    @tests.keys.each do |key|
        tests_to_run << key.to_s
    end
elsif !@format
    i = 1
    group_names = @groups.keys.sort_by { |s| s.to_s }
    @groups.keys.each do |g|
        group_name = g.to_s
        puts "\033[0;36m#{ group_name == " " ? "Ungrouped tests" : group_name
        }\033[;0m"
                           group_tests = @groups[g].sort_by { |s| s.to_s }
                           group_tests.each do |t|
                               puts "  #{ i }) #{ t.to_s }"
                               i += 1
                           end
    end
    print "Which tests to run: "
    resp = gets.chomp
    tests_to_run = parse_test_string resp, test_names
end 

tests_to_run.each do |test_name|
    run_test( test_name, @tests[test_name.to_sym] )
    puts ""
end
