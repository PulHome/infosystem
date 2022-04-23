<root>
<inputFile name="input.xml">
<![CDATA[
	<xml>
	<MyStoreDescription>This is my best store!
	</MyStoreDescription>
	<MyBooksCollection value="NotForResale"/>
	<BookStore>
		<Book Author="Pushkin" Name="Captains Daughter"/>
		<Book Author="Lermontov" Name="Mtsyri"/>
	</BookStore>
	</xml>
]]>
</inputFile>
<inputFile name="settings.xml">
<![CDATA[
	<settings>
		<array name="BookStore"/>
		<attributeName value="Author" />
	<settings/>
]]>
</inputFile>
</root>