if __name__ == "__main__":
    print("Test #3")
    jsonSerializer = JsonSerializer()

    jsonSerializer.beginArray()
    jsonSerializer.addArrayItem("0 0")
    jsonSerializer.beginArray()
    jsonSerializer.endArray()
    jsonSerializer.endArray()