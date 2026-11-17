package cn.zhuatech.citationlocator;

import java.util.*;

/**
 * 只把唯一命中引文纳入覆盖，按来源合并重叠或相邻的 Unicode 码点区间。每个来源保留前 12 个空白区间及完整计数；覆盖不等同于事实准确性。 上海如静知华信息科技有限公司 ·
 * https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Diagnostics {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static Map<String, Object> map(Object... pairs) {
    var m = new LinkedHashMap<String, Object>();
    for (int i = 0; i < pairs.length; i += 2) m.put((String) pairs[i], pairs[i + 1]);
    return m;
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static double num(Map<String, Object> m, String k) {
    return ((Number) m.get(k)).doubleValue();
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static Double ratio(double a, double b) {
    return b == 0 ? null : a / b;
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @SuppressWarnings("unchecked")
  static Map<String, Object> evaluate(
      Map<String, Object> cfg, List<Map<String, Object>> rows, Map<String, Object> base) {
    var metrics = (Map<String, Object>) base.get("metrics");
    var evidence = (List<Map<String, Object>>) base.get("rows");
    var details = new ArrayList<Map<String, Object>>();
    var docs = new TreeMap<String, String>();
    var spans = new HashMap<String, List<int[]>>();
    for (var row : rows) docs.put((String) row.get("sourceId"), (String) row.get("source"));
    for (var r : evidence)
      if ("唯一命中".equals(r.get("match")))
        spans
            .computeIfAbsent((String) r.get("sourceId"), k -> new ArrayList<>())
            .add(new int[] {(int) num(r, "fromChar"), (int) num(r, "toChar")});
    long all = 0, used = 0;
    for (var doc : docs.entrySet()) {
      int size = doc.getValue().codePointCount(0, doc.getValue().length()),
          end = 0,
          cover = 0,
          gaps = 0;
      var sample = new ArrayList<List<Integer>>();
      var ranges = spans.getOrDefault(doc.getKey(), new ArrayList<>());
      ranges.sort(Comparator.comparingInt(x -> x[0]));
      for (var range : ranges) {
        int a = range[0], b = range[1];
        if (a > end) {
          gaps++;
          if (sample.size() < 12) sample.add(List.of(end, a));
        }
        cover += Math.max(0, b - Math.max(a, end));
        end = Math.max(end, b);
      }
      if (end < size) {
        gaps++;
        if (sample.size() < 12) sample.add(List.of(end, size));
      }
      all += size;
      used += cover;
      details.add(
          map(
              "sourceId",
              doc.getKey(),
              "sourceLength",
              size,
              "coveredCodepoints",
              cover,
              "unambiguousCoverage",
              (double) cover / size,
              "gapCount",
              gaps,
              "uncoveredSample",
              sample,
              "gapsTruncated",
              gaps > sample.size()));
    }
    return Checks.result(
        map(
            "sourceDocs",
            docs.size(),
            "coveredCodepoints",
            used,
            "unambiguousCoverage",
            (double) used / all),
        details);
  }
}
