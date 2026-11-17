package cn.zhuatech.citationlocator;

import static cn.zhuatech.citationlocator.Checks.*;

import java.util.*;

/**
 * 逐条将回答中的引文与给定来源正文精确对齐，输出首匹配字符区间，区分唯一命中、歧义及缺证。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Algorithm {
  /**
   * 偏移为 Unicode 码点、左闭右开。只检查引文是否存在，不判断语义或事实真伪。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  static Map<String, Object> compute(Map<String, Object> cfg, List<Map<String, Object>> rows) {
    var base = calculate(cfg, rows);
    var enriched = new LinkedHashMap<String, Object>(base);
    enriched.put("inspection", Inspection.evaluate(cfg, rows, base));
    enriched.put("diagnostics", Diagnostics.evaluate(cfg, rows, base));
    return enriched;
  }

  /**
   * 保留原始计算口径。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  private static Map<String, Object> calculate(
      Map<String, Object> cfg, List<Map<String, Object>> rows) {
    must(rows != null && !rows.isEmpty() && rows.size() <= 200, "需要 1–200 行");
    unique(rows);
    var evidence = new ArrayList<Map<String, Object>>();
    int min = integer(cfg, "minChars", 1, 1000), one = 0, many = 0, none = 0;
    var docs = new HashMap<String, String>();
    for (var r : rows) {
      String src = str(r, "source", false),
          q = str(r, "quote", false),
          key = str(r, "sourceId", false);
      String old = docs.putIfAbsent(key, src);
      must(old == null || old.equals(src), "同一来源标识对应了不同正文");
      int len = q.codePointCount(0, q.length());
      must(len >= min, "引文过短");
      int a = src.indexOf(q);
      boolean repeated = a >= 0 && src.indexOf(q, a + Character.charCount(src.codePointAt(a))) >= 0;
      if (a < 0) none++;
      else if (repeated) many++;
      else one++;
      int start = a < 0 ? -1 : src.codePointCount(0, a);
      evidence.add(
          Map.of(
              "id",
              r.get("id"),
              "sourceId",
              key,
              "fromChar",
              start,
              "toChar",
              a < 0 ? -1 : start + len,
              "match",
              a < 0 ? "缺证" : repeated ? "多处命中" : "唯一命中"));
    }
    return result(Map.of("uniqueHits", one, "ambiguous", many, "missing", none), evidence);
  }
}
