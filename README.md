# Enhanced Block Definitions

This extension provides additional capabilities to engine extension blocks.

Currently, this extension only provides the ability to use a `code` field type that accepts text input which is treated as pure code.

Here's an example of creating our own version of the inline code block. We define the block as normal in `blocks.xml`, but for the field type, `text` is specified.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<blocks>
	
	<block
		tag="our-code-short"
		spec="Code: %0"
		help="Run arbitrary (Haxe) code."
		code='~'
		type="normal"
		color="gray"
		returns="anything">
		<fields>
			<text order="0" /> <!-- code: see blocks-enhancements.xml -->
		</fields>
	</block>
	
</blocks>
```

Then, we add a new file, `blocks-enhancements.xml`, alongside `blocks.xml`. Here, we specify which block and which field to overwrite with the `code` type.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<blocks-enhancements>
	
	<block
		tag="our-code-short">
		<fields>
			<code order="0" />
		</fields>
	</block>
	
</blocks-enhancements>
```