# Мария Евсеева
# Принадлежит ли точка области?

def isPointInArea(xVal, yVal):
    pointIsLowerFirstLine = yVal < ((2 * xVal) + 2)
    pointIsLowerSecondLine = yVal < -xVal
    pointIsOutCircle = ((xVal + 1)**2 + (yVal - 1)**2) >= 4
    pointIsInCircle = ((xVal + 1)**2 + (yVal - 1)**2) <= 4
    return (pointIsLowerFirstLine and pointIsLowerSecondLine\
            and pointIsOutCircle) or ((not pointIsLowerFirstLine)\
            and (not pointIsLowerSecondLine) and pointIsInCircle)

xInputVal = float(input("Введите x: "))
yInputVal = float(input("Введите y: "))

if isPointInArea(xInputVal, yInputVal):
    print("YES")
else:
    print("NO")
