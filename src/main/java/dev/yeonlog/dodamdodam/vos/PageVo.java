package dev.yeonlog.dodamdodam.vos;

import lombok.Getter;

@Getter
public class PageVo {
    private final int requestPage;
    private final int totalCount;
    private final int pageSize;    // 페이지당 항목 수 (10)
    private final int blockSize;   // 페이지 블록 수 (5)

    private final int minPage;
    private final int maxPage;
    private final int startPage;
    private final int endPage;

    public PageVo(int requestPage, int totalCount, int pageSize, int blockSize) {
        this.requestPage = requestPage;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.blockSize = blockSize;

        this.minPage = 1;
        this.maxPage = (int) Math.ceil((double) totalCount / pageSize);

        int currentBlock = (int) Math.ceil((double) requestPage / blockSize);
        this.startPage = (currentBlock - 1) * blockSize + 1;
        this.endPage = Math.min(startPage + blockSize - 1, maxPage);
    }

    public int getOffset() {
        return (requestPage - 1) * pageSize;
    }
}
