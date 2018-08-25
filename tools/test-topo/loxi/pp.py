# Copyright 2013, Big Switch Networks, Inc.

"""
pp - port of Ruby's PP library
Also based on Lindig, C., & GbR, G. D. (2000). Strictly Pretty.

Example usage:
>>> import pp.pp as pp
>>> print pp([[1, 2], [3, 4]], maxwidth=15)
[
  [ 1, 2 ],
  [ 3, 4 ]
]
"""
import unittest
from contextlib import contextmanager

def pp(obj, maxwidth=79):
    """
    Pretty-print the given object.
    """
    ctx = PrettyPrinter(maxwidth=maxwidth)
    ctx.pp(obj)
    return str(ctx)


## Pretty-printers for builtin classes

def pretty_print_list(pp, obj):
    with pp.group():
        pp.text('[')
        with pp.indent(2):
            for v in obj:
                if not pp.first(): pp.text(',')
                pp.breakable()
                pp.pp(v)
        pp.breakable()
        pp.text(']')

def pretty_print_dict(pp, obj):
    with pp.group():
        pp.text('{')
        with pp.indent(2):
            for (k, v) in sorted(obj.items()):
                if not pp.first(): pp.text(',')
                pp.breakable()
                pp.pp(k)
                pp.text(': ')
                pp.pp(v)
        pp.breakable()
        pp.text('}')

pretty_printers = {
    list: pretty_print_list,
    dict: pretty_print_dict,
}


## Implementation

class PrettyPrinter(object):
    def __init__(self, maxwidth):
        self.maxwidth = maxwidth
        self.cur_indent = 0
        self.root_group = Group()
        self.group_stack = [self.root_group]

    def current_group(self):
        return self.group_stack[-1]

    def text(self, s):
        self.current_group().append(str(s))

    def breakable(self, sep=' '):
        self.current_group().append(Breakable(sep, self.cur_indent))

    def first(self):
        return self.current_group().first()

    @contextmanager
    def indent(self, n):
        self.cur_indent += n
        yield
        self.cur_indent -= n

    @contextmanager
    def group(self):
        self.group_stack.append(Group())
        yield
        new_group = self.group_stack.pop()
        self.current_group().append(new_group)

    def pp(self, obj):
        if hasattr(obj, "pretty_print"):
            obj.pretty_print(self)
        elif type(obj) in pretty_printers:
            pretty_printers[type(obj)](self, obj)
        else:
            self.text(repr(obj))

    def __str__(self):
        return self.root_group.render(0, self.maxwidth)

class Group(object):
    __slots__ = ["fragments", "length", "_first"]

    def __init__(self):
        self.fragments = []
        self.length = 0
        self._first = True

    def append(self, x):
        self.fragments.append(x)
        self.length += len(x)

    def first(self):
        if self._first:
            self._first = False
            return True
        return False

    def __len__(self):
        return self.length

    def render(self, curwidth, maxwidth):
        dobreak = len(self) > (maxwidth - curwidth)

        a = []
        for x in self.fragments:
            if isinstance(x, Breakable):
                if dobreak:
                    a.append('\n')
                    a.append(' ' * x.indent)
                    curwidth = 0
                else:
                    a.append(x.sep)
            elif isinstance(x, Group):
                a.append(x.render(curwidth, maxwidth))
            else:
                a.append(x)
            curwidth += len(a[-1])
        return ''.join(a)

class Breakable(object):
    __slots__ = ["sep", "indent"]

    def __init__(self, sep, indent):
        self.sep = sep
        self.indent = indent

    def __len__(self):
        return len(self.sep)


## Tests

class TestPP(unittest.TestCase):
    def test_scalars(self):
        self.assertEquals(pp(1), "1")
        self.assertEquals(pp("foo"), "'foo'")

    def test_hash(self):
        expected = """{ 1: 'a', 'b': 2 }"""
        self.assertEquals(pp(eval(expected)), expected)
        expected = """\
{
  1: 'a',
  'b': 2
}"""
        self.assertEquals(pp(eval(expected), maxwidth=0), expected)

    def test_array(self):
        expected = """[ 1, 'a', 2 ]"""
        self.assertEquals(pp(eval(expected)), expected)
        expected = """\
[
  1,
  'a',
  2
]"""
        self.assertEquals(pp(eval(expected), maxwidth=0), expected)

    def test_nested(self):
        expected = """[ [ 1, 2 ], [ 3, 4 ] ]"""
        self.assertEquals(pp(eval(expected)), expected)
        expected = """\
[
  [
    1,
    2
  ],
  [
    3,
    4
  ]
]"""
        self.assertEquals(pp(eval(expected), maxwidth=0), expected)

    def test_breaking(self):
        expected = """\
[
  [ 1, 2 ],
  'abcdefghijklmnopqrstuvwxyz'
]"""
        self.assertEquals(pp(eval(expected), maxwidth=24), expected)
        expected = """\
[
  [ 'abcd', 2 ],
  [ '0123456789' ],
  [
    '0123456789',
    'abcdefghij'
  ],
  [ 'abcdefghijklmnop' ],
  [
    'abcdefghijklmnopq'
  ],
  { 'k': 'v' },
  {
    1: [ 2, [ 3, 4 ] ],
    'foo': 'abcdefghijklmnop'
  }
]"""
        self.assertEquals(pp(eval(expected), maxwidth=24), expected)
        expected = """\
[
  [ 1, 2 ],
  [ 3, 4 ]
]"""
        self.assertEquals(pp(eval(expected), maxwidth=15), expected)

    # This is an edge case where our simpler algorithm breaks down.
    @unittest.expectedFailure
    def test_greedy_breaking(self):
        expected = """\
abc def
ghijklmnopqrstuvwxyz\
"""
        pp = PrettyPrinter(maxwidth=8)
        pp.text("abc")
        with pp.group():
            pp.breakable()
        pp.text("def")
        with pp.group():
            pp.breakable()
        pp.text("ghijklmnopqrstuvwxyz")
        self.assertEquals(str(pp), expected)

if __name__ == '__main__':
    unittest.main()
