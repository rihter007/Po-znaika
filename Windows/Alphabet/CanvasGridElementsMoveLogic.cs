using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace ru.po_znaika.alphabet
{
    interface IAllowElementSelect
    {
        bool IsSelectionAllowed(UIElement element);
    }

    class GridDescription
    {
        public Grid grid;
        public int firstRowIndex;
        public int lastRowIndex;
        public int firstColumnIndex;
        public int lastColumnIndex;

        public UIElement[][] gridElements;
        public bool isSelfMoveAllowed;
    }

    class RectangleArea
    {
        public Point topLeftVertex;
        public double width;
        public double height;
    }

    class CanvasGridElementMoveLogic
    {
        private struct CoordinateDelta
        {
            public CoordinateDelta(int x, int y)
            {
                X = x;
                Y = y;
            }

            public int X;
            public int Y;
        }

        private class GridElementCoordinates
        {
            public GridElementCoordinates(int colIndex, int rIndex)
            {
                columnIndex = colIndex;
                rowIndex = rIndex;
            }

            public int columnIndex;
            public int rowIndex;
        }

        private class SelectedGridElement
        {
            public SelectedGridElement(GridDescription gr, GridElementCoordinates elementCoordinates)
            {
                gridDescription = gr;
                selectedElement = elementCoordinates;
            }

            public GridDescription gridDescription;
            public GridElementCoordinates selectedElement;
        }

        public CanvasGridElementMoveLogic(Canvas paintCanvas, RectangleArea canvasMoveArea, IEnumerable<GridDescription> grids,
            Brush noSelectionBrush, Brush selectionBrush, IAllowElementSelect elementSelection)
        {
            m_canvas = paintCanvas;
            m_allowedCanvasMoveArea = canvasMoveArea;
            m_canvasGrids = grids;
            
            m_noSelectionBrush = noSelectionBrush;
            m_selectionBrush = selectionBrush;
            m_allowElementSelection = elementSelection;

            NullMoveActionItems();
        }        

        public void OnMouseLeftButtonDown(Point mouseClickPosition)
        {
            RevertMoveAction();

            SelectedGridElement selectedGridElement = GetGridElementByCoord((int)mouseClickPosition.X, (int)mouseClickPosition.Y);
            if (selectedGridElement == null)
                return;

            UIElement selectedElement = selectedGridElement.gridDescription.gridElements[selectedGridElement.selectedElement.rowIndex - selectedGridElement.gridDescription.firstRowIndex][selectedGridElement.selectedElement.columnIndex - selectedGridElement.gridDescription.firstColumnIndex];
            if (!ElementCanBeSelected(selectedElement))
                return;

            selectedGridElement.gridDescription.grid.Children.Remove(selectedElement);
            m_canvas.Children.Add(selectedElement);

            int gridX = (int)(double)selectedGridElement.gridDescription.grid.GetValue(Canvas.LeftProperty);
            int gridY = (int)(double)selectedGridElement.gridDescription.grid.GetValue(Canvas.TopProperty);

            double gridColumnWidth = selectedGridElement.gridDescription.grid.ActualWidth / selectedGridElement.gridDescription.grid.ColumnDefinitions.Count;
            double gridRowHeight = selectedGridElement.gridDescription.grid.ActualHeight / selectedGridElement.gridDescription.grid.RowDefinitions.Count;
            
            m_coordinateDelta = new CoordinateDelta(
                (int)(mouseClickPosition.X - (gridX + selectedGridElement.selectedElement.columnIndex * gridColumnWidth)),
                (int)(mouseClickPosition.Y - (gridY + selectedGridElement.selectedElement.rowIndex * gridRowHeight)));

            selectedElement.SetValue(Canvas.LeftProperty, mouseClickPosition.X - m_coordinateDelta.X);
            selectedElement.SetValue(Canvas.TopProperty, mouseClickPosition.Y - m_coordinateDelta.Y);

            m_sourceGrid = selectedGridElement.gridDescription;
            m_sourceGridElementColumnIndex = selectedGridElement.selectedElement.columnIndex;
            m_sourceGridElementRowIndex = selectedGridElement.selectedElement.rowIndex;
            
            m_movedElement = selectedElement;
        }

        public void OnMouseMove(Point mousePosition)
        {
            if (!IsPointInMoveArea(mousePosition))
            {
                RevertMoveAction();
                return;
            }

            if ((m_movedElement == null) || ((mousePosition.X < m_coordinateDelta.X) || (mousePosition.Y < m_coordinateDelta.Y)))
            {
                if (m_lastNavigationElement != null)
                    Helpers.PaintUiElement(m_lastNavigationElement, m_noSelectionBrush);
                m_lastNavigationElement = null;
                return;
            }

            // Process navigation 
            {
                SelectedGridElement selectedGridElement = GetGridElementByCoord((int)mousePosition.X, (int)mousePosition.Y);
                if ((selectedGridElement != null) && (!((selectedGridElement.gridDescription == m_sourceGrid) && (!selectedGridElement.gridDescription.isSelfMoveAllowed))))
                {
                    UIElement currentNavigatedElement = selectedGridElement.gridDescription.gridElements[selectedGridElement.selectedElement.rowIndex - selectedGridElement.gridDescription.firstRowIndex][selectedGridElement.selectedElement.columnIndex - selectedGridElement.gridDescription.firstColumnIndex];
                    if (currentNavigatedElement != m_lastNavigationElement)
                    {
                        if (m_lastNavigationElement != null)
                            Helpers.PaintUiElement(m_lastNavigationElement, m_noSelectionBrush);

                        Helpers.PaintUiElement(currentNavigatedElement, m_selectionBrush);
                        m_lastNavigationElement = currentNavigatedElement;
                    }
                }
                else
                {
                    if (m_lastNavigationElement != null)
                        Helpers.PaintUiElement(m_lastNavigationElement, m_noSelectionBrush);
                    m_lastNavigationElement = null;
                }
            }

            m_movedElement.SetValue(Canvas.LeftProperty, mousePosition.X - m_coordinateDelta.X);
            m_movedElement.SetValue(Canvas.TopProperty, mousePosition.Y - m_coordinateDelta.Y);
        }

        public void OnMouseLeftButtonUp(Point mouseClickPosition)
        {
            if (!IsPointInMoveArea(mouseClickPosition))
            {
                RevertMoveAction();
                return;
            }

            if (m_movedElement == null)
            {
                RevertMoveAction();
                return;
            }

            SelectedGridElement targetGridElement = GetGridElementByCoord((int)mouseClickPosition.X, (int)mouseClickPosition.Y);
            if (targetGridElement == null)
            {
                RevertMoveAction();
                return;
            }

            GridDescription targetGrid = targetGridElement.gridDescription;
            if ((targetGrid == m_sourceGrid) && (!targetGrid.isSelfMoveAllowed))
            {
                RevertMoveAction();
                return;
            }

            m_canvas.Children.Remove(m_movedElement);

            UIElement targetElement = targetGrid.gridElements[targetGridElement.selectedElement.rowIndex - targetGrid.firstRowIndex][targetGridElement.selectedElement.columnIndex - targetGrid.firstColumnIndex];

            if (targetGrid == m_sourceGrid)
            {
                m_sourceGrid.grid.Children.Add(m_movedElement);
            }
            else
            {
                targetGrid.grid.Children.Add(m_movedElement);

                targetGrid.grid.Children.Remove(targetElement);
                m_sourceGrid.grid.Children.Add(targetElement);
            }

            m_sourceGrid.gridElements[m_sourceGridElementRowIndex - m_sourceGrid.firstRowIndex][m_sourceGridElementColumnIndex - m_sourceGrid.firstColumnIndex] = targetElement;
            targetGrid.gridElements[targetGridElement.selectedElement.rowIndex - targetGrid.firstRowIndex][targetGridElement.selectedElement.columnIndex - targetGrid.firstColumnIndex] = m_movedElement;

            targetElement.SetValue(Grid.ColumnProperty, m_sourceGridElementColumnIndex);
            targetElement.SetValue(Grid.RowProperty, m_sourceGridElementRowIndex);

            m_movedElement.SetValue(Grid.ColumnProperty, targetGridElement.selectedElement.columnIndex);
            m_movedElement.SetValue(Grid.RowProperty, targetGridElement.selectedElement.rowIndex);

            if (m_lastNavigationElement != null)
                Helpers.PaintUiElement(m_lastNavigationElement, m_noSelectionBrush);
            m_lastNavigationElement = null;

            NullMoveActionItems();
        }
                
        private bool IsPointInMoveArea(Point p)
        {
            if (m_allowedCanvasMoveArea == null)
                return true;

            return ((p.X >= m_allowedCanvasMoveArea.topLeftVertex.X) && (p.X <= m_allowedCanvasMoveArea.topLeftVertex.X + m_allowedCanvasMoveArea.width) &&
                (p.Y >= m_allowedCanvasMoveArea.topLeftVertex.Y) && (p.Y <= m_allowedCanvasMoveArea.topLeftVertex.Y + m_allowedCanvasMoveArea.height));
        }

        public void RevertMoveAction()
        {
            if (m_movedElement != null)
            {
                m_canvas.Children.Remove(m_movedElement);
                m_sourceGrid.grid.Children.Add(m_movedElement);

                m_movedElement.SetValue(Grid.ColumnProperty, m_sourceGridElementColumnIndex);
                m_movedElement.SetValue(Grid.RowProperty, m_sourceGridElementRowIndex);

                if (m_lastNavigationElement != null)
                    Helpers.PaintUiElement(m_lastNavigationElement, m_noSelectionBrush);
            }

            NullMoveActionItems();
        }

        private void NullMoveActionItems()
        {
            m_movedElement = null;
            m_lastNavigationElement = null;
            m_sourceGrid = null;
            m_sourceGridElementColumnIndex = m_sourceGridElementRowIndex = -1;
            m_coordinateDelta.X = m_coordinateDelta.Y = 0;
        }
        
        private SelectedGridElement GetGridElementByCoord(int xCoord, int yCoord)
        {
            SelectedGridElement result = null;

            foreach (var gridDescription in m_canvasGrids)
            {
                GridElementCoordinates currentGridElement = GetSingleGridElementByCoord(gridDescription.grid, xCoord, yCoord);
                if ((currentGridElement != null) && ((currentGridElement.columnIndex >= gridDescription.firstColumnIndex) && (currentGridElement.columnIndex <= gridDescription.lastColumnIndex)) &&
                    ((currentGridElement.rowIndex >= gridDescription.firstRowIndex) && (currentGridElement.rowIndex <= gridDescription.lastRowIndex)))
                {
                    result =  new SelectedGridElement(gridDescription, currentGridElement);
                    break;
                }
            }

            return result;
        }

        private bool ElementCanBeSelected(UIElement elem)
        {
            if (m_allowElementSelection != null)
                return m_allowElementSelection.IsSelectionAllowed(elem);
            return true;
        }

        private static GridElementCoordinates GetSingleGridElementByCoord(Grid grid, int xCoord, int yCoord)
        {
            int gridX = (int)(double)grid.GetValue(Canvas.LeftProperty);
            int gridY = (int)(double)grid.GetValue(Canvas.TopProperty);            

            // Check that we pointed out of grid
            if (((xCoord < gridX) || (xCoord > gridX + grid.Width)) ||
                ((yCoord < gridY) || (yCoord > gridY + grid.Height)))
                return null;

            double columnWidth = grid.ActualWidth / grid.ColumnDefinitions.Count;
            double rowWidth = grid.ActualHeight / grid.RowDefinitions.Count;

            return new GridElementCoordinates((int)((xCoord - gridX) / columnWidth), (int)((yCoord - gridY) / rowWidth));
        }

        private Brush m_noSelectionBrush;
        private Brush m_selectionBrush;

        private Canvas m_canvas;
        private RectangleArea m_allowedCanvasMoveArea;
        private IEnumerable<GridDescription> m_canvasGrids;
        private IAllowElementSelect m_allowElementSelection;

        private UIElement m_lastNavigationElement;
        private UIElement m_movedElement;
        private CoordinateDelta m_coordinateDelta;
        private GridDescription m_sourceGrid;
        private int m_sourceGridElementColumnIndex;
        private int m_sourceGridElementRowIndex;
    }
}
