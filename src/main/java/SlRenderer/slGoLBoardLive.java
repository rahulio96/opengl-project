package SlRenderer;

public class slGoLBoardLive extends slGoLBoard {
    public slGoLBoardLive(int numRows, int numCols) {
        super(numRows, numCols);
    }

    public slGoLBoardLive(int numRows, int numCols, int numAlive) {
        super(numRows, numCols, numAlive);
    }

    @Override
    public int countLiveTwoDegreeNeighbors(int row, int col) {
        int count = 0;

        int prevRow = (row - 1 + NUM_ROWS) % (NUM_ROWS);
        int prevCol = (col - 1 + NUM_COLS) % (NUM_COLS);
        int nextRow = (row + 1) % NUM_ROWS;
        int nextCol = (col + 1) % NUM_COLS;

        // all directions we need to move in
        int [][] directions = {{nextRow, col}, {prevRow, col}, {row, nextCol}, {row, prevCol},
                {nextRow, nextCol}, {prevRow, prevCol}, {nextRow, prevCol}, {prevRow, nextCol}};
        for (int[] direction : directions) {
            int new_r = direction[0], new_c = direction[1];

            // see if cell is alive or not
            if (liveCellArray[new_r][new_c]) {
                count++;
            }
        }
        return count;
    }

    // given by professor
    @Override
    public int updateNextCellArray() {
        int retVal = 0;

        int nln = 0;  // Number Live Neighbors
        boolean ccs = true; // Current Cell Status
        for (int row = 0; row < NUM_ROWS; ++row){
            for (int col = 0; col < NUM_COLS; ++col) {
                ccs = liveCellArray[row][col];
                nln = countLiveTwoDegreeNeighbors(row, col);
                if (!ccs && nln == 3) {
                    nextCellArray[row][col] = true;
                    ++retVal;
                } else {
                    // Current Cell Status is true
                    if (nln < 2 || nln > 3) {
                        nextCellArray[row][col] = false;
                    } else {
                        // nln == 2 || nln == 3
                        nextCellArray[row][col] = true;
                        ++retVal;
                    }
                }
            }  // for (int row = 0; ...)
        }  //  for (int col = 0; ...)

        boolean[][] tmp = liveCellArray;
        liveCellArray = nextCellArray;
        nextCellArray = tmp;

        return retVal;
    }  //  int updateNextCellArray()
}
