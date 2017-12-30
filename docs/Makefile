NAME := manual

BIB_FILES := $(NAME).bbl

OTHER_BIB := ${shell echo *.bib | grep -v '*'}

# this is more dangerous -- it assumes that all .tex files are
# important to building this document
TEX_FILES := ${shell echo *.tex }

FIG_BASE := figures

EPS_FILES := ${shell echo $(FIG_BASE)/*.fig | grep -v '*' | sed 's@\.fig@.eps@g' }
PDF_FILES := ${shell echo $(FIG_BASE)/*.fig | grep -v '*' | sed -e 's@\.fig@.pdf@g'}

# add the names of file that are eps source -- be sure 
# to put them on both lines, so pdflatex and latex work
# fine.
EPS_ONLY := 
PDF_ONLY := 


all: pdf ps

ps: $(NAME).dvi
	dvips $(NAME) -o $(NAME).ps

$(PDF_ONLY): $(EPS_ONLY)
	epstopdf $< -o=$@

$(EPS_FILES): %.eps: %.fig
	@echo Building $@
	@fig2dev -L eps $< $@

$(PDF_FILES): %.pdf: %.fig
	@echo Building $@
	@fig2dev -L pdf -p dummy $< $@

pdf:  $(TEX_FILES) $(BIB_FILES) $(PDF_FILES) $(PDF_ONLY)
	pdflatex $(NAME)
	pdflatex $(NAME)

$(NAME).dvi: $(TEX_FILES) $(BIB_FILES) $(EPS_FILES) $(EPS_ONLY)
	latex $(NAME)
	latex $(NAME)

$(NAME).bbl: $(OTHER_BIB) $(PDF_FILES)
	pdflatex $(NAME)
	bibtex $(NAME)

clean:
	rm -f *.aux *.log *.dvi *.toc *.ps $(NAME).pdf *~ *.bbl *.blg 

veryclean: clean
	rm -f $(PDF_FILES) $(EPS_FILES) $(PDF_ONLY) $(FIG_BASE)/*.bak
