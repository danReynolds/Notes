#!/usr/bin/env ruby
puts "Enter File Name"
file = gets.chomp
puts "Assemble or dissassemble? A/D"
assembly = gets.chomp
if assembly.capitalize == "A"
  a = File.open(file, "r")
  contents = a.read
  a = a.reopen(file, "w+")
  a.puts contents.scan(/.{1,10}/).each_with_index.map{|a,i| ".word 0x" + a}
elsif assembly.capitalize == "D"
  a = File.open(file, "r")
  contents = a.read
  a = a.reopen(file, "w+")
  a.puts contents.split(" ").reject{|a| a.start_with?("0x")}.join(" ")
end
