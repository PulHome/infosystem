#Литвинов Илья
#Расписание звонков
#20.10.18

print("Введите номер урока: ")
a = int(input())
a = a*45 + (a//2)*5 + ((a+1)//2-1)*15
print ("Время окончания урока: ")
print(a//60 + 9, a%60)