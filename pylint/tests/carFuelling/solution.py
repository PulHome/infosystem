import sys

ERROR_CODE = -1

MIN_D = 1
MAX_D = 100000
MIN_M = 1
MAX_M = 400
MIN_N = 1
MAX_N = 300


def input_int_in_range(min_number: int, max_number: int):
    int_number = int(input())
    if int_number < min_number or int_number > max_number:
        raise ValueError
    return int_number


def input_sort_int_arr(inStr, size: int, except_start_value: int, except_end_value: int):
    arr_string = inStr.split()
    if len(arr_string) != size:
        raise ValueError
    arr = list(map(int, arr_string))
    for num in arr:
        if num <= except_start_value or num >= except_end_value:
            raise ValueError
        except_start_value = num
    return arr


def count_number_stops(stop_points: list, max_distance: int):
    number_stops = 0
    distance = max_distance
    is_possible_stop = False
    current_point = 0
    ind = 0
    while ind < len(stop_points):
        if distance < stop_points[ind] - current_point:
            if is_possible_stop:
                distance = max_distance
                number_stops += 1
                is_possible_stop = False
            else:
                return -1
        else:
            distance -= stop_points[ind] - current_point
            is_possible_stop = True
            current_point = stop_points[ind]
            ind += 1
    return number_stops


def solveDistanceTask(d, m, n, stopsStr):
    d: int
    m: int
    n: int
    stops: list
    try:
        stops = input_sort_int_arr(stopsStr, n, 0, d)
        stops.append(d)
    except ValueError:
        print(ERROR_CODE)

    return count_number_stops(stops, m)


if __name__ == '__main__':
    res = solveDistanceTask(10, 3, 4, "1 2 5 8")
    print(res)