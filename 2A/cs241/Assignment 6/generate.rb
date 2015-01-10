#!/usr/bin/env ruby

a = 65
alphabet = ["{","}","(",")","=","!","<",">","+","-","*","/","%",",",";","[","]","&"]
states = ["START","ID","NUM","NOT","LGE","FINISH","ERROR"]
accepted = ["ID","NUM","LGE","FINISH"]
transitions = []
alpha = []
numbers = ((0..9).step(1)).map(&:to_s)

while a < 91 do
  alpha << a.chr
  a = a + 1;
end
a = 97
while a < 123 do
  alpha << a.chr
  a = a + 1;
end

alphabet = alphabet + alpha + numbers

states.each do |state|
  alphabet.each do |letter|
    if state == states[0]
      if ["{","}","[","]","(",")","%","&","+","-","*","0","/",",",";"].include? letter
        transitions << state + " " + letter  + " " + states[5]
      elsif alpha.include? letter
        transitions << state + " " + letter + " " + states[1]
      elsif letter == "!"
        transitions << state + " " + letter + " " + states[3]
      elsif letter == "<" || letter == ">" || letter == "="
        transitions << state + " " + letter  + " " + states[4]
      elsif numbers.unshift.include? letter
        transitions << state + " " + letter + " " + states[2]
      else
        puts "YOU FUCKED UP" + state + letter
      end
    elsif state == states[1]
      if (alpha.include? letter) || (numbers.include? letter)
        transitions << state + " " + letter + " " + state
      else
        transitions << state + " " + letter + " " + states[6]
      end
    elsif state == states[2] 
      if numbers.include? letter
        transitions << state + " " + letter + " " + state
      else
        transitions << state + " " + letter + " " + states[6]
      end
    elsif state == states[3]
      if letter == "="
        transitions << state + " " + letter + " " + states[5]
      else
        transitions << state + " " + letter + " " + states[6]
      end
    elsif state == states[4]
      if letter == "="
        transitions << state + " " + letter + " " + states[5]
      else
        transitions << state + " " + letter + " " + states[6]
      end
    elsif state == states[5]
      transitions << state + " " + letter + " " + states[6]
    end
  end
end

file = "wlpp.dfa"
a = File.open(file, "w+")
a.puts alphabet.length
a.puts alphabet
a.puts states.length
a.puts states
a.puts "START"
a.puts accepted.length
a.puts accepted
a.puts transitions.length
a.puts transitions

