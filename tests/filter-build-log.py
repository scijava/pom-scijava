#
# filter-build-log.py - Cuts out cruft to focus on build failure details.
#

# This script filters build logs down to only [WARNING] and [ERROR] lines,
# and consolidates lengthy duplicate class listings down to packages only.

import sys

def print_filtered_log(log):
    dups = []
    parsingdups = False
    atbeginning = True
    for line in log:
        line = line.rstrip('\n')
        if line.startswith('[INFO]'):
            # Filter out non-error build messages.
            continue
        if line.startswith('Download') or line.startswith('Progress'):
            # Filter out details of remote resource queries.
            continue
        if atbeginning and not line.strip():
            # Filter out leading blank lines.
            continue
        atbeginning = False
        if parsingdups:
            if line.startswith('    '):
                if line.index('/') >= 0:
                    # Strip to containing package only.
                    line = line[:line.rindex('/')]
                dups.append(line)
            else:
                parsingdups = False
                for dup in sorted(set(dups)):
                    print(dup)
                print('')
                dups = []
        else:
            if line == '  Duplicate classes:':
                print('  Duplicate packages:')
                parsingdups = True
            else:
                print(line)

for arg in sys.argv[1:]:
    with open(arg) as f:
        print_filtered_log(f)

