#  С помощью регулярных выражений найдите в строке дважды подряд повторяющиеся слова.
#  Удалите эти повторы, распечатайне строку без повторов.

import re

s = input()
print(re.sub(r'\b(\w+)\b(.+)\b(\1)\b', r'\1', s))