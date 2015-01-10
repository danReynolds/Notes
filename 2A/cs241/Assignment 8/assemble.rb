#!/usr/bin/env ruby
file = "sample2.lr1"
# a = File.open(file, "w+")
b = File.open(file, "r")
contents = []
while !b.eof?
  contents << b.readline
end
c = 1
while c < 35
  puts "terms.add(\"#{contents[c].strip}\");"
  c = c+1
end
c=36
while c < 49
  puts "nonterms.add(\"#{contents[c].strip}\");"
  c = c+1
end
c=52
while c < 89
  puts "prods.add(\"#{contents[c].strip}\");"
  puts "productionRules.add(\"#{contents[c].strip}\");"

  c = c+1
end
c=92
while c < 801
  if contents.include? "shift"
  lines = contents[c].split(" ")
  puts "table.add(\"#{contents[c].strip}\");"
  c = c+1
end


