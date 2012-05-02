#!/usr/bin/env python

import datetime
import os
import sys

CLASS_TMPL = """
// Auto-generated by tag-build.py -- DO NOT EDIT
package org.kegbot.kegtap.build;

public final class BuildInfo {

  public static final String BUILD_DATE_HUMAN = "%(BUILD_DATE_HUMAN)s";

  public static final String BUILD_TAGS = "%(BUILD_TAGS)s";

  public static final boolean RELEASE_BUILD = %(RELEASE_BUILD)s;

}
"""

def usage():
  sys.stderr.write("Usage: %s <filename.xml>\n" % sys.argv[0])
  sys.exit(1)

def main():
  if len(sys.argv) < 2:
    usage()

  now = datetime.datetime.now()

  build_strings = {
    'RELEASE_BUILD' : 'false',
    'BUILD_DATE_HUMAN' : now.strftime('%Y%m%d-%H%M%S'),
    'BUILD_TAGS' : 'dev',
  }

  out_class = os.path.join(sys.argv[1], 'src/org/kegbot/kegtap/build/BuildInfo.java')
  f = open(out_class, 'w')
  f.write(CLASS_TMPL % build_strings)
  f.close()

  out_res = os.path.join(sys.argv[1], 'res/values/build_strings.xml')
  f = open(out_res, 'w')
  f.write('<?xml version="1.0" encoding="utf-8"?>\n')
  f.write('<resources>\n')
  for k, v in build_strings.iteritems():
    f.write('  <string name="%s">%s</string>\n' % (k, str(v)));
  f.write('</resources>\n')
  f.close()

if __name__ == '__main__':
  main()