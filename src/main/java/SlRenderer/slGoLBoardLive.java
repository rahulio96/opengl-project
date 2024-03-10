package csc133;

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
        // all directions we need to move in
        int [][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] direction : directions) {
            int new_r = row + direction[0], new_c = col + direction[1];

            // wrap around if out of bounds
            if (new_r < 0) {
                new_r = NUM_ROWS - 1;
            }
            if (new_c < 0) {
                new_c = NUM_COLS - 1;
            }
            if (new_r >= NUM_ROWS) {
                new_r = 0;
            }
            if (new_c >= NUM_COLS) {
                new_c = 0;
            }

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
