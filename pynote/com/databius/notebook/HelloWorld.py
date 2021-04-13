def parse(i):
    o = ''
    if len(i) > 0:
        if i[0] == ')':
            o = f'(){parse(i[1:])}'
        elif i[- 1] == '(':
            o = f'{parse(i[:- 1])}()'
        else:
            first = i.find(')')
            if first > 0:
                o = f'{parse(i[1:first - 1] + i[first + 1:])}'
    return o


print(f'()() -> {parse("()()")}')
print(f'(() -> {parse("(()")}')
print(f'))()( -> {parse("))()(")}')
