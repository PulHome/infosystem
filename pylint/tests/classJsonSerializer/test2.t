if __name__ == "__main__":
    print("Test #2")
    jsonSerializer = JsonSerializer()
    assert isinstance(jsonSerializer, Serializer) 
    jsonSerializer.beginArray()
    jsonSerializer.beginArray()
    jsonSerializer.endArray()
    jsonSerializer.endArray()
