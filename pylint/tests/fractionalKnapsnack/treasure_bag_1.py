import sys

VALUE_ERROR_CODE = -1
INDEX_ERROR_CODE = -2
ACCURACY = 4

VALUE_IND = 0
WEIGHT_IND = 1


def convert_to_positive_int(number: str):
    if int(number) < 0:
        raise ValueError
    return int(number)


def get_item_unit_value(item: list[int, int]):
    return item[VALUE_IND] / item[WEIGHT_IND]


def pop_best_item(items: list[list]):
    if len(items) == 0:
        return -1
    item_index = 0
    max_unit_value = 0
    for i in range(len(items)):
        unit_value = get_item_unit_value(items[i])
        if max_unit_value < unit_value:
            max_unit_value = unit_value
            item_index = i
    return items.pop(item_index)


def optimal_total_value(items: list[list], bag_weight):
    total_value = 0
    items_copy = items.copy()
    while bag_weight > 0:
        best_item = pop_best_item(items_copy)
        if best_item == -1:
            break
        if bag_weight >= best_item[WEIGHT_IND]:
            total_value += best_item[VALUE_IND]
        else:
            total_value += best_item[VALUE_IND] \
                           * (bag_weight / best_item[WEIGHT_IND])
        bag_weight -= best_item[WEIGHT_IND]
    return total_value


def solveLootTask(lootSize, bagSize, loot):
    items_total: int
    max_weight: int
    treasures: list

    items_total = lootSize
    max_weight = bagSize

    input_strs = [loot[i].split() for i in range(items_total)]
    treasures = [[convert_to_positive_int(input_strs[i][0]),
                  convert_to_positive_int(input_strs[i][1])]
                 for i in range(items_total)]

    return round(optimal_total_value(treasures, max_weight), ACCURACY)


if __name__ == '__main__':
    solveLootTask(lootSize=2, bagSize=1, loot=["1 1",])
