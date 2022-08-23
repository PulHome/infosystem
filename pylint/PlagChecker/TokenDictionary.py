import keyword
import tokenize

operationsToken = {
    'MOP': ['+', '-', '*', '/', '%', '**', '//'],
    'COP': ['==', '>', '<', '>=', '<=', '!=', '<>'],
    'AOP': ['=', '+=', '-=', '*=', '/=', '%=', '**=', '//='],
    'BOP': ['&', '|', '^', '<<', '>>', '~']
}

namesToken = {
    'CYCLE': ['for', 'while'],
    'COND': ['if', 'elif', 'else'],
    'TRANSIT': ['break', 'continue', 'return'],
    'LOP': ['and', 'or', 'not'],
    'OOP': ['in', 'is']
}

extraToken = [tokenize.ENCODING, tokenize.ENDMARKER,
              tokenize.COMMENT, tokenize.NEWLINE,
              tokenize.NL, tokenize.INDENT, tokenize.DEDENT]


def FormatTokenStr(tokensStr):
    resultStr = []

    for token in tokensStr:
        tokenType = token.type

        if tokenType == tokenize.NAME:
            if keyword.iskeyword(token.string):
                for key, values in namesToken.items():
                    if token.string in values:
                        resultStr.append(key)
                        break
                else:
                    resultStr.append('KEY')
            else:
                resultStr.append(f"{tokenize.tok_name[tokenType]}")

        elif tokenType == tokenize.OP:
            for key, values in operationsToken.items():
                if token.string in values:
                    resultStr.append(key)
        elif tokenType in extraToken:
            continue
        else:
            resultStr.append(f"{tokenize.tok_name[tokenType]}")

    return resultStr
