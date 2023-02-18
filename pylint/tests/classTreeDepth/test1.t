if __name__ == "__main__":
    print("Test #1")
    jsonSerializer = JsonSerializer()
    assert isinstance(jsonSerializer, Serializer) 
    jsonSerializer.beginArray()
    jsonSerializer.endArray()