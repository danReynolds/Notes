#!/usr/bin/env ruby
i = 1
transitions = []
transitions << "start nickel nickel1"
transitions << "start dime nickel2"
transitions << "start quarter nickel5"
transitions << "start loonie nickel20"

a = File.new("coins.dfa", "w+")

states = []
while i < 25 do
  states << "nickel#{i}"
  transitions << "nickel#{i} nickel nickel#{(i+1 <= 25)? (i+1):"overflow"}"
  transitions << "nickel#{i} dime nickel#{(i+2 <= 25)? (i+2):"overflow"}"
  transitions << "nickel#{i} quarter nickel#{(i+5 <= 25)? (i+5):"overflow"}"
  transitions << "nickel#{i} loonie nickel#{(i+20 <= 25)? (i+20):"overflow"}"
  i=i+1
end

states << "nickeloverflow"
transitions << "nickel25 nickel nickeloverflow"
transitions << "nickel25 dime nickeloverflow"
transitions << "nickel25 quarter nickeloverflow"
transitions << "nickel25 loonie nickeloverflow"

a.puts states
a.puts transitions


