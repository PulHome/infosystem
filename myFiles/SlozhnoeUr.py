# Аверина Мария
# Сложное уравнение
# 12.09.2018

a = int(input("Введите a: "))
b = int(input("Введите b: "))
c = int(input("Введите c: "))
d = int(input("Введите d: "))

if (c == 0 and d == 0) or (a == 0 and b != 0) or b / a == d / c:
    print("NO")
elif a == 0 and b == 0:
    print("INF")
else:
    res = int(-b / a)
    if (res != 0):
        print("Ответ: ", res)
    else:
        print("NO")