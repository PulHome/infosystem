# Аверина Мария
# Перестановка слов
# 02.10.2018

a = input("Введите исходную строку: ")

i = a.index(' ')
a = a[i + 1:] + ' ' + a[:i]

print("Строка с переставленными словами: ", a)