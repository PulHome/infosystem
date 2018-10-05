# powered by Maltsev Alexey
# 08.09.18
# Король делает ход по шахматной доске, доказать что ход возможен

import math

ax = input('Введите значение х от 1 до 8 для первой точки :')
if ax.isdigit():
    ax = int(ax)
    if ax < 1 or ax > 8:
        print("error")
else:
    ax = int(input('Введите значение х от 1 до 8 для первой точки повторно :'))

ay = input('Введите значение y от 1 до 8 для первой точки :')
if ay.isdigit():
    ay = int(ay)
    if ay < 1 or ay > 8:
        print("error")
else:
    ay = int(input('Введите значение y от 1 до 8 для первой точки повторно :'))

bx = input('Введите значение х от 1 до 8 для второй точки :')
if bx.isdigit():
    bx = int(bx)
    if bx < 1 or bx > 8:
        print("error")
else:
    bx = input('Введите значение х от 1 до 8 для второй точки повторно :')

by = input('Введите значение y от 1 до 8 для второй точки :')
if by.isdigit():
    by = int(by)
    if by < 1 or by > 8:
        print("error")
else:
    by = input('Введите значение y от 1 до 8 для второй точки повторно :')

dX = ax - bx
dY = ay - by
res = math.fabs(dX)+ math.fabs(dY) #сложение модулей разности значений по Х и У
#print(res)  #проверка результата
if res > 0 and res < 3:
    print('YES')
else:
    print('NO')

#        1 2 3 4 5 6 7 8            Точка А - местоположение Короля
#       1_|_|_|_|_|_|_|_|           Точка Б - Желаемая точка перемещения
#       2_|_|_|_|_|_|_|_|           А и Б задаются координатами (ОХ и ОУ)
#       3_|_|*|*|*|_|_|_|           Решение:
#       4_|_|*|+|*|_|_|_|           Модуль разницы между точками по координатам равен
#       5_|_|*|*|*|_|_|_|           [1.0] [0.1] [1.1] - удовлетворяет решению
#       6_|_|_|_|_|_|_|_|           +________________
#       7_|_|_|_|_|_|_|_|             1     1     2
#       8_|_|_|_|_|_|_|_|                 1<res<2
#
#