#!/usr/bin/env python3
"""
Converts Lucide SVG icons into Android VectorDrawable XML for Compose Multiplatform resources.

Why: DESIGN §7 specifies Lucide at 24/20 with a 1.75 stroke. Compose Multiplatform renders Android XML vectors on every
platform but not SVG, so the bundled icons are XML. Only the icons in ICONS are converted; the Kotlin `AppIcon` set mirrors
this list and mirrors directional icons in right-to-left layouts (the XML carries no autoMirrored flag).

Usage:
    python3 -I tooling/scripts/lucide_to_vector.py <lucide-static/icons dir> <output drawable dir>

Source: the `lucide-static` npm package (ISC licence, see core/designsystem/licenses/LICENSE-Lucide.txt).
Re-run after bumping the Lucide version recorded in core/designsystem/licenses/SOURCES.md.
"""
from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

STROKE_WIDTH = "1.75"
SVG_NS = "{http://www.w3.org/2000/svg}"

# Icons that also ship a filled variant (`ic_<name>_filled`): only the saved state (DESIGN §7).
FILLED = ["bookmark-check"]

# Lucide name → drawable name (ic_<name> with underscores). One icon per meaning; DESIGN §7 plus the v5 screens.
ICONS = [
    "chevron-right", "chevron-left", "chevron-down", "chevron-up", "x", "search", "settings", "bell", "user-round",
    "book-open", "wheat", "scale", "book-open-text", "door-open", "users-round", "bookmark", "bookmark-check", "list",
    "play", "pause", "list-start", "share-2", "copy", "volume-2", "skip-forward", "skip-back", "repeat-1", "mic",
    "radio", "quote", "message-circle-question", "lightbulb", "sunrise", "sunset", "compass", "external-link",
    "download", "wifi-off", "check", "circle-check", "clock", "ellipsis-vertical", "flag", "heart", "message-circle",
    "grip-vertical", "pencil", "trash-2", "plus", "minus", "maximize", "minimize", "moon", "sun", "monitor", "map-pin",
    "refresh-cw", "circle-alert", "info", "log-out", "timer", "gauge", "sparkles", "circle-x", "send", "image",
    "search-x",
]


def num(value: str) -> float:
    return float(value)


def fmt(value: float) -> str:
    text = f"{value:.3f}".rstrip("0").rstrip(".")
    return text if text else "0"


def circle_path(cx: float, cy: float, r: float) -> str:
    return (
        f"M{fmt(cx - r)},{fmt(cy)} a{fmt(r)},{fmt(r)} 0 1,0 {fmt(2 * r)},0 "
        f"a{fmt(r)},{fmt(r)} 0 1,0 {fmt(-2 * r)},0"
    )


def ellipse_path(cx: float, cy: float, rx: float, ry: float) -> str:
    return (
        f"M{fmt(cx - rx)},{fmt(cy)} a{fmt(rx)},{fmt(ry)} 0 1,0 {fmt(2 * rx)},0 "
        f"a{fmt(rx)},{fmt(ry)} 0 1,0 {fmt(-2 * rx)},0"
    )


def rect_path(x: float, y: float, w: float, h: float, rx: float, ry: float) -> str:
    if rx == 0 and ry == 0:
        return f"M{fmt(x)},{fmt(y)} h{fmt(w)} v{fmt(h)} h{fmt(-w)} z"
    rx = min(rx, w / 2)
    ry = min(ry, h / 2)
    return (
        f"M{fmt(x + rx)},{fmt(y)} H{fmt(x + w - rx)} A{fmt(rx)},{fmt(ry)} 0 0 1 {fmt(x + w)},{fmt(y + ry)} "
        f"V{fmt(y + h - ry)} A{fmt(rx)},{fmt(ry)} 0 0 1 {fmt(x + w - rx)},{fmt(y + h)} H{fmt(x + rx)} "
        f"A{fmt(rx)},{fmt(ry)} 0 0 1 {fmt(x)},{fmt(y + h - ry)} V{fmt(y + ry)} "
        f"A{fmt(rx)},{fmt(ry)} 0 0 1 {fmt(x + rx)},{fmt(y)} Z"
    )


def points_path(points: str, close: bool) -> str:
    values = [num(v) for v in re.split(r"[\s,]+", points.strip()) if v]
    pairs = [f"{fmt(values[i])},{fmt(values[i + 1])}" for i in range(0, len(values), 2)]
    return "M" + " L".join(pairs) + (" Z" if close else "")


def element_to_path(element: ET.Element) -> str | None:
    tag = element.tag.replace(SVG_NS, "")
    a = element.attrib
    if tag == "path":
        return a["d"]
    if tag == "circle":
        return circle_path(num(a["cx"]), num(a["cy"]), num(a["r"]))
    if tag == "ellipse":
        return ellipse_path(num(a["cx"]), num(a["cy"]), num(a["rx"]), num(a["ry"]))
    if tag == "line":
        return f"M{fmt(num(a['x1']))},{fmt(num(a['y1']))} L{fmt(num(a['x2']))},{fmt(num(a['y2']))}"
    if tag == "rect":
        rx = num(a.get("rx", a.get("ry", "0")))
        ry = num(a.get("ry", a.get("rx", "0")))
        return rect_path(num(a.get("x", "0")), num(a.get("y", "0")), num(a["width"]), num(a["height"]), rx, ry)
    if tag == "polyline":
        return points_path(a["points"], close=False)
    if tag == "polygon":
        return points_path(a["points"], close=True)
    if tag in ("g", "title", "desc"):
        return None
    raise SystemExit(f"Unsupported SVG element <{tag}>")


def convert(svg_file: Path, filled: bool = False) -> str:
    root = ET.parse(svg_file).getroot()
    view_box = root.attrib.get("viewBox", "0 0 24 24").split()
    paths = []
    for element in root.iter():
        if element is root:
            continue
        data = element_to_path(element)
        if data:
            paths.append(data)
    if not paths:
        raise SystemExit(f"{svg_file.name}: no drawable elements")
    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        f"<!-- Generated from Lucide '{svg_file.stem}'{' (filled)' if filled else ''} by "
        "tooling/scripts/lucide_to_vector.py. Do not edit. -->",
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '    android:width="24dp"',
        '    android:height="24dp"',
        f'    android:viewportWidth="{view_box[2]}"',
        f'    android:viewportHeight="{view_box[3]}">',
    ]
    fill = "#FF000000" if filled else "#00000000"
    for data in paths:
        lines += [
            "    <path",
            f'        android:pathData="{data}"',
            f'        android:fillColor="{fill}"',
            '        android:strokeColor="#FF000000"',
            f'        android:strokeWidth="{STROKE_WIDTH}"',
            '        android:strokeLineCap="round"',
            '        android:strokeLineJoin="round" />',
        ]
    lines.append("</vector>")
    return "\n".join(lines) + "\n"


def main(argv: list[str]) -> None:
    if len(argv) != 3:
        raise SystemExit(__doc__)
    source = Path(argv[1])
    target = Path(argv[2])
    target.mkdir(parents=True, exist_ok=True)
    for name in ICONS:
        svg = source / f"{name}.svg"
        if not svg.exists():
            raise SystemExit(f"Missing Lucide icon: {svg}")
        (target / f"ic_{name.replace('-', '_')}.xml").write_text(convert(svg), encoding="utf-8")
    for name in FILLED:
        svg = source / f"{name}.svg"
        (target / f"ic_{name.replace('-', '_')}_filled.xml").write_text(convert(svg, filled=True), encoding="utf-8")
    print(f"Converted {len(ICONS)} icons and {len(FILLED)} filled variants into {target}")


if __name__ == "__main__":
    main(sys.argv)
