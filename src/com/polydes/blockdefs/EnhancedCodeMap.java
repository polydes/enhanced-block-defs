package com.polydes.blockdefs;

import java.util.Map.Entry;

import stencyl.sw.editors.snippet.designer.Definition;
import stencyl.sw.editors.snippet.designer.codebuilder.CodeBuilder;
import stencyl.sw.editors.snippet.designer.codebuilder.CodeElement;
import stencyl.sw.editors.snippet.designer.codebuilder.CodeElementList;
import stencyl.sw.editors.snippet.designer.codebuilder.SpecialCodeElement;
import stencyl.sw.editors.snippet.designer.codebuilder.SpecialCodeElement.HashReplaceElement;
import stencyl.sw.editors.snippet.designer.codemap.BasicCodeMap;

public class EnhancedCodeMap implements CodeElement
{
	protected CodeElementList codeBase;
	private String[] fieldReplacements;
	
	public EnhancedCodeMap(Definition baseDef, DefinitionAdditions additions)
	{
		this.codeBase = CodeElementList.asList(((BasicCodeMap) baseDef.codeGenerator).getCode());
		
		if(!additions.fieldReplacements.isEmpty())
		{
			fieldReplacements = new String[baseDef.fields.length];
			
			for(Entry<Integer, String> fr : additions.fieldReplacements.entrySet())
			{
				this.fieldReplacements[fr.getKey()] = fr.getValue();
			}
		}
	}

	@Override
	public void toCode(CodeBuilder builder)
	{
		if(fieldReplacements == null)
		{
			for(CodeElement e : codeBase)
			{
				e.toCode(builder);
			}
		}
		else
		{
			Definition def = builder.peekBlock().getDefinition();
			String realTag = def.tag;
			for(CodeElement e : codeBase)
			{
				int fieldIndex = -1;
				if(e == SpecialCodeElement.TILDE)
					fieldIndex = builder.currentFieldIndex();
				else if(e == SpecialCodeElement.BACK_TICK)
					fieldIndex = 0;
				else if(e instanceof HashReplaceElement)
					fieldIndex = Integer.parseInt(((HashReplaceElement) e).toString().substring("!#".length()));
				
				if(fieldIndex != -1 && fieldReplacements[fieldIndex] != null && fieldReplacements[fieldIndex].equals("code"))
				{
					def.tag = "code-short";
					e.toCode(builder);
					def.tag = realTag;
					continue;
				}
				
				e.toCode(builder);
			}
		}
	}
}
