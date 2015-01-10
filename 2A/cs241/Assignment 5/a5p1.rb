#!/usr/bin/env ruby
puts "Enter File Name"
file = gets.chomp
puts "Assemble or dissassemble? A/D"
assembly = gets.chomp
if assembly.capitalize == "A"
  a = File.open(file, "r")
  contents = a.read
  a = a.reopen(file, "w+")
  a.puts contents.scan(/.{1,10}/).each_with_index.map{|a,i| "0x#{((i*4).to_s(16)).rjust(8,"0")} " + a}
elsif assembly.capitalize == "D"
  a = File.open(file, "r")
  contents = a.read
  a = a.reopen(file, "w+")
  a.puts contents.split(" ").reject{|a| a.start_with?("0x")}.join(" ")
end
