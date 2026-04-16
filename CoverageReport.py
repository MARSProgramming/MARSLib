import xml.etree.ElementTree as ET
import sys

def parse_jacoco_report(xml_path):
    tree = ET.parse(xml_path)
    root = tree.getroot()
    all_covered = True
    
    for package in root.findall('package'):
        for class_tag in package.findall('class'):
            name = class_tag.get('name')
            for counter in class_tag.findall('counter'):
                if counter.get('type') == 'INSTRUCTION':
                    missed = int(counter.get('missed'))
                    if missed > 0:
                        all_covered = False
                        source_name = class_tag.get('sourcefilename')
                        source_files = package.findall('sourcefile')
                        for sourcefile in source_files:
                            if sourcefile.get('name') == source_name:
                                missed_lines = [line.get('nr') for line in sourcefile.findall('line') if int(line.get('mi')) > 0]
                                print(f"{name} missed {missed} instructions on lines: {missed_lines}")
                                break
    if all_covered:
        print("100% COVERAGE ACHIEVED!")

parse_jacoco_report('build/reports/jacoco/test/jacocoTestReport.xml')
