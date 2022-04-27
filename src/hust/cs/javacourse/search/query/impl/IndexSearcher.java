package hust.cs.javacourse.search.query.impl;


import hust.cs.javacourse.search.index.AbstractPosting;
import hust.cs.javacourse.search.index.AbstractPostingList;
import hust.cs.javacourse.search.index.AbstractTerm;
import hust.cs.javacourse.search.index.impl.Index;

import hust.cs.javacourse.search.index.impl.Posting;
import hust.cs.javacourse.search.index.impl.Term;
import hust.cs.javacourse.search.query.AbstractHit;
import hust.cs.javacourse.search.query.AbstractIndexSearcher;
import hust.cs.javacourse.search.query.Sort;
import hust.cs.javacourse.search.util.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IndexSearcher extends AbstractIndexSearcher {
    /**
     * 从指定索引文件打开索引，加载到index对象里.先打开索引再执行search方法
     * @param indexFile ：指定索引文件
     */
    @Override
    public void open(String indexFile) {
        this.index = new Index();
        try {
            index.load(new File(indexFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据单个检索词进行搜索
     * @param queryTerm ：检索词
     * @param sorter ：排序器
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm, Sort sorter) {
        // 忽略大小写
        if (Config.IGNORE_CASE) {
            queryTerm.setContent(queryTerm.getContent().toLowerCase());
        }
        AbstractPostingList indexSearchResult = index.search(queryTerm);
        if (indexSearchResult == null) {
            return new Hit[0];
        }
        List<AbstractHit> result = new ArrayList<>();
        for (int i = 0; i < indexSearchResult.size(); i++) {
            AbstractPosting posting = indexSearchResult.get(i);

            AbstractHit hit = new Hit(posting.getDocId(), index.getDocName(posting.getDocId()));
            hit.getTermPostingMapping().put(queryTerm, posting);
            hit.setScore(sorter.score(hit));
            result.add(hit);
        }

        sorter.sort(result);
        AbstractHit[] returnResult = new AbstractHit[result.size()];
        return result.toArray(returnResult);
    }

    /**
     *
     * 根据二个检索词进行搜索
     * @param queryTerm1 ：第1个检索词
     * @param queryTerm2 ：第2个检索词
     * @param sorter ：    排序器
     * @param combine ：   多个检索词的逻辑组合方式
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm1, AbstractTerm queryTerm2, Sort sorter, LogicalCombination combine) {
        AbstractPostingList indexSearchResult1 = index.search(queryTerm1);
        AbstractPostingList indexSearchResult2 = index.search(queryTerm2);
        // 如果两个都没找到直接就是空的数组
        if (indexSearchResult1 == null && indexSearchResult2 == null) {
            return new Hit[0];
        }
        List<AbstractHit> result = new ArrayList<>();
        if (combine == LogicalCombination.AND) {
            // 如果有一个词语根本就不存在，那就直接返回空的数组
            if (indexSearchResult1 == null || indexSearchResult2 == null) {
                return new Hit[0];
            }
            // 求交集
            for (int i = 0; i < indexSearchResult1.size(); i++) {
                // 获取docId
                int docId = indexSearchResult1.get(i).getDocId();
                int sub_index = indexSearchResult2.indexOf(docId);
                if (sub_index != -1) {
                    AbstractHit hit = new Hit(docId, index.getDocName(docId));
                    hit.getTermPostingMapping().put(queryTerm1, indexSearchResult1.get(i));
                    hit.getTermPostingMapping().put(queryTerm2, indexSearchResult2.get(sub_index));
                    hit.setScore(sorter.score(hit));
                    result.add(hit);
                }
            }
        } else if (combine == LogicalCombination.OR) {
            // 如果有一个词语不存在直接退化为对另外一个词语的搜索
            if (indexSearchResult1 == null) {
                return search(queryTerm2, sorter);
            }
            if (indexSearchResult2 == null) {
                return search(queryTerm1, sorter);
            }

            for (int i = 0; i < indexSearchResult1.size(); i++) {
                // 首先添加
                int docId = indexSearchResult1.get(i).getDocId();
                int sub_index = indexSearchResult2.indexOf(docId);
                if (sub_index == -1) {
                    // 如果在另外一个词语中没有,那就正常添加
                    AbstractHit hit = new Hit(docId, index.getDocName(docId));
                    hit.getTermPostingMapping().put(queryTerm1, indexSearchResult1.get(i));
                    hit.setScore(sorter.score(hit));
                    result.add(hit);
                } else {
                    // 如果在另外一个中有, 那就要做一些修改
                    AbstractHit hit = new Hit(docId, index.getDocName(docId));
                    hit.getTermPostingMapping().put(queryTerm1, indexSearchResult1.get(i));
                    hit.getTermPostingMapping().put(queryTerm2, indexSearchResult2.get(sub_index));
                    hit.setScore(sorter.score(hit));
                    result.add(hit);
                }
            }
            for (int i = 0; i < indexSearchResult2.size(); i++) {
                int docId = indexSearchResult2.get(i).getDocId();
                int sub_index = indexSearchResult1.indexOf(docId);
                if (sub_index == -1) {
                    // 只有当1中不存在的时候才添加
                    AbstractHit hit = new Hit(docId, index.getDocName(docId));
                    hit.getTermPostingMapping().put(queryTerm2, indexSearchResult2.get(i));
                    hit.setScore(sorter.score(hit));
                    result.add(hit);
                }
            }
        }

        sorter.sort(result);
        AbstractHit[] returnResult = new AbstractHit[result.size()];
        return result.toArray(returnResult);
    }
    /**
     * 查询两个在文中相邻出现的单词（进阶功能）
     * @param queryTerm1 ：第一个单词
     * @param queryTerm2 ：第二个单词
     * @param sorter ：排序器
     * @return ：查询结果数组
     */
    public AbstractHit[] search(AbstractTerm queryTerm1, AbstractTerm queryTerm2, Sort sorter) {
        AbstractPostingList postList1 = index.search(queryTerm1);
        AbstractPostingList postList2 = index.search(queryTerm2);
        if(postList1 == null || postList2 == null) return null;
        List<AbstractHit> hitArray = new ArrayList<AbstractHit>();
        int i=0, j=0;
        while(i < postList1.size() && j < postList2.size()) {
            AbstractPosting post1 = postList1.get(i);
            AbstractPosting post2 = postList2.get(j);
            // 这里默认索引中的数据都是按文档ID从小到大排序了
            if (post1.getDocId() == post2.getDocId()) {
                List<Integer> pos1 = post1.getPositions();
                List<Integer> pos2 = post2.getPositions();
                int a = 0, b = 0;
                List<Integer> positions = new ArrayList<Integer>();     // 存放连续两个单词出现的位置
                while(a < pos1.size() && b < pos2.size()){
                    int p1 = pos1.get(a);
                    int p2 = pos2.get(b);
                    if(p1 == p2-1){
                        positions.add(p1);
                        a++;    b++;
                    } else if(p1 < p2-1){
                        a++;
                    } else {
                        b++;
                    }
                }
                if(positions.size() > 0) {      // 否则会出现score = 0.0的情况
                    String path = index.getDocName(post1.getDocId());
                    Map<AbstractTerm, AbstractPosting> mp =
                            new HashMap<AbstractTerm, AbstractPosting>();
                    mp.put(new Term(queryTerm1.getContent() + " " + queryTerm2.getContent()),
                            new Posting(post1.getDocId(), positions.size(), positions));
                    AbstractHit h = new Hit(post1.getDocId(), path, mp);
                    h.setScore(sorter.score(h));        // 先设置分数
                    hitArray.add(h);
                }
                i++;    j++;
            } else if (post1.getDocId() < post2.getDocId()) {
                i++;
            } else {        // post1 > post2
                j++;
            }
        }
        if(hitArray.size() < 1) return null;
        new SimpleSorter().sort(hitArray);
        return hitArray.toArray(new Hit[0]);
    }
}
