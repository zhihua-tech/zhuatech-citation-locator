package cn.zhuatech.citationlocator;

import static cn.zhuatech.citationlocator.Checks.*;

import java.util.*;

/**
 * 返回每条引文的准确出现次数，以及前 20 个候选的 Unicode 码点起始偏移。允许重叠匹配；超过 20 处仍完整计数并明确提示截断。 上海如静知华信息科技有限公司 ·
 * https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Inspection {
  /**
   * 有序结果，可表达无法估计的数值。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  private static Map<String, Object> fields(Object... pairs) {
    var row = new LinkedHashMap<String, Object>();
    for (int i = 0; i < pairs.length; i += 2) row.put((String) pairs[i], pairs[i + 1]);
    return row;
  }

  /**
   * 对已校验的输入生成复核证据。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  @SuppressWarnings("unchecked")
  static Map<String, Object> evaluate(
      Map<String, Object> cfg, List<Map<String, Object>> rows, Map<String, Object> base) {
    var details = new ArrayList<Map<String, Object>>();

    int truncated = 0;
    for (var r : rows) {
      String s = (String) r.get("source"), q = (String) r.get("quote");
      int count = 0, offset = 0, prev = 0;
      var hits = new ArrayList<Integer>();
      for (int i = s.indexOf(q);
          i >= 0;
          i = s.indexOf(q, i + Character.charCount(s.codePointAt(i)))) {
        if (count < 20) {
          offset += s.codePointCount(prev, i);
          hits.add(offset);
          prev = i;
        }
        count++;
      }
      if (count > 20) truncated++;
      details.add(
          fields(
              "id",
              r.get("id"),
              "sourceId",
              r.get("sourceId"),
              "occurrences",
              count,
              "offsets",
              hits,
              "truncated",
              count > 20));
    }
    return result(fields("previewLimit", 20, "truncatedQuotes", truncated), details);
  }
}
